package amf.apicontract.internal.validation.payload

import amf.apicontract.internal.validation.payload.collector.{CollectorsRunner, ValidationCandidateCollector}
import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.metamodel.document.PayloadFragmentModel
import amf.core.internal.plugins.payload.ErrorFallbackValidationPlugin
import amf.core.internal.validation.{ValidationCandidate, ValidationConfiguration}
import amf.shapes.internal.validation.definitions.ShapePayloadValidations
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.SchemaException

import scala.concurrent.{ExecutionContext, Future}

case class UnitPayloadsValidation(baseUnit: BaseUnit, collectors: Seq[ValidationCandidateCollector]) {

  val candidates: Seq[ValidationCandidate] = CollectorsRunner(collectors).traverse(baseUnit).toSeq

  val index: DataNodeIndex = {
    val nodes = getCandidateNodes
    DataNodeIndex(nodes)
  }

  private def getCandidateNodes = {
    candidates
      .filter(_.payload.fields.exists(PayloadFragmentModel.Encodes))
      .map(_.payload.encodes) ++ candidates.map(_.shape).distinct.flatMap(_.values)
  }

  def validate(config: ValidationConfiguration)(
      implicit executionContext: ExecutionContext): Future[Seq[AMFValidationResult]] = {
    val nextConfig = config.amfConfig.withPlugin(ErrorFallbackValidationPlugin(SeverityLevels.WARNING))
    CandidateValidator.validateAll(candidates, config.copy(nextConfig)).map(groupResults)
  }

  private def groupResults(report: AMFValidationReport): Seq[AMFValidationResult] = {
    // we can order here or order in each json schema validator pair when ask for the exception causes

    val indexedResults: Map[String, List[AMFValidationResult]] = report.results.toList.sorted.groupBy { r =>
      r.targetNode
    }

    val payloadResults = index.aggregate(indexedResults)
    val schemaResults = indexedResults
      .filter {
        case (_, validations) =>
          validations.exists(v =>
            v.validationId == SchemaException.id || v.validationId == ShapePayloadValidations.UntranslatableDraft2019Fields.id)
      }
      .flatMap(_._2)
    payloadResults ++ schemaResults
  }

}

sealed case class DataNodeEntry(d: DataNode, sonsKeys: Seq[String]) {

  def aggregate(indexedResult: Map[String, List[AMFValidationResult]]): Option[AMFValidationResult] = {
    val sonsResults: Seq[AMFValidationResult] = collectResults(indexedResult)

    indexedResult.get(d.id) match {
      case Some(rootResults) if sonsResults.nonEmpty => Some(buildRootResult(rootResults, sonsToString(sonsResults)))
      case Some(rootResult)                          => Some(buildRootResult(rootResult, None))
      case None if sonsResults.nonEmpty => // there is not root result? create empty result?
        Some(
          AMFValidationResult(
            sonsToString(sonsResults).getOrElse(""),
            if (sonsResults.exists(_.severityLevel == SeverityLevels.VIOLATION)) SeverityLevels.VIOLATION
            else SeverityLevels.WARNING,
            d.id,
            sonsResults.head.targetProperty,
            sonsResults.head.validationId, // ??
            d.position(),
            d.location(),
            null
          ))
      case _ => None // not results for the dataNode, ignore
    }
  }

  private def sorted(elements: Seq[AMFValidationResult]) = elements.sorted // need order by something of the targetNode

  private def buildRootResult(rootResults: List[AMFValidationResult], additionalMessage: Option[String]) = {
    val sortedResults = sorted(rootResults)
    sortedResults match {
      case rootResult :: Nil if additionalMessage.isDefined =>
        rootResult.copy(message = rootResult.message + "\n" + additionalMessage.getOrElse(""))
      case rootResult :: Nil => rootResult
      case _ :: _ =>
        val severity =
          if (sortedResults.exists(_.severityLevel == SeverityLevels.VIOLATION)) SeverityLevels.VIOLATION
          else SeverityLevels.WARNING
        var messages = ""
        sortedResults.map(_.message).distinct.foreach { r =>
          messages = messages + r + "\n"
        }
        val finalMessage = additionalMessage.fold(messages)({ a =>
          messages + a
        })
        AMFValidationResult(
          finalMessage,
          severity,
          sortedResults.head.targetNode,
          Option(d.id),
          sortedResults.head.validationId, //?
          d.position(),
          d.location(),
          null
        )
    }
  }

  private def collectResults(indexedResults: Map[String, Seq[AMFValidationResult]]): Seq[AMFValidationResult] =
    sorted(sonsKeys.flatMap { k =>
      indexedResults.getOrElse(k, Nil)
    })

  private def sonsToString(sonsResult: Seq[AMFValidationResult]): Option[String] = {
    var message = ""
    sonsResult.map(_.message).distinct.foreach { r =>
      message = message.concat(r + "\n")
    }
    Option(message)
  }
}

sealed case class DataNodeIndex(private val dataNodes: Seq[DataNode]) {

  private val index: Map[String, DataNodeEntry] = dataNodes.map { d =>
    d.id -> DataNodeEntry(d, indexSons(d))
  }.toMap

  private def indexSons(dataNode: DataNode): Seq[String] = {
    dataNode match {
      case obj: ObjectNode => obj.allProperties().flatMap(p => p.id +: indexSons(p)).toSeq
      case arr: ArrayNode =>
        arr.members.flatMap(m => { m.id +: indexSons(m) })
      case _ => Nil // if other type, dont have childrens, and the root id is indexed at data node entry level.
    }
  }

  def aggregate(indexedResult: Map[String, List[AMFValidationResult]]): Seq[AMFValidationResult] =
    index.flatMap { case (id, entry) => entry aggregate indexedResult }.toSeq
}
