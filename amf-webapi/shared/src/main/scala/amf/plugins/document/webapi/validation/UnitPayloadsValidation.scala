package amf.plugins.document.webapi.validation

import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{ArrayNode, DataNode, ObjectNode}
import amf.core.remote.Platform
import amf.core.services.PayloadValidator
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels, ValidationCandidate}

import scala.concurrent.Future

case class UnitPayloadsValidation(baseUnit: BaseUnit, platform: Platform) {

  import scala.concurrent.ExecutionContext.Implicits.global

  val candidates: Seq[ValidationCandidate] = ExamplesCandidatesCollector(baseUnit) ++
    ShapeFacetsCandidatesCollector(baseUnit, platform) ++
    AnnotationsCandidatesCollector(baseUnit, platform)

  val index = DataNodeIndex(candidates.map(_.payload.encodes))

  def validate(): Future[Seq[AMFValidationResult]] =
    PayloadValidator.validateAll(candidates, SeverityLevels.WARNING).map(groupResults)

  private def groupResults(report: AMFValidationReport): Seq[AMFValidationResult] = {
    val indexedResults: Map[String, Seq[AMFValidationResult]] = report.results.groupBy { r =>
      r.targetNode
    }
    index.aggregate(indexedResults)
  }

}

sealed case class DataNodeEntry(d: DataNode, sonsKeys: Seq[String]) {

  def aggregate(indexedResult: Map[String, Seq[AMFValidationResult]]): Option[AMFValidationResult] = {
    val sonsResults: Seq[AMFValidationResult] = collectResults(indexedResult)

    indexedResult.get(d.id) match {
      case Some(rootResults) if sonsResults.nonEmpty => Some(buildRootResult(rootResults, sonsToString(sonsResults)))
      case Some(rootResult)                          => Some(buildRootResult(rootResult, None))
      case None if sonsResults.nonEmpty => // there is not root result? create empty result?
        Some(
          AMFValidationResult(
            sonsToString(sonsResults).getOrElse(""),
            if (sonsResults.exists(_.level == SeverityLevels.VIOLATION)) SeverityLevels.VIOLATION
            else SeverityLevels.WARNING,
            d.id,
            sonsResults.head.targetProperty,
            sonsResults.head.validationId, // ??
            d.annotations.find(classOf[LexicalInformation]),
            null
          ))
      case _ => None // not results for the dataNode, ignore
    }
  }

  private def sorted(elements: Seq[AMFValidationResult]) = elements.sorted // need order by something of the targetNode

  private def buildRootResult(rootResults: Seq[AMFValidationResult], additionalMessage: Option[String]) = {
    val sortedResults = sorted(rootResults)
    sortedResults match {
      case rootResult :: Nil if additionalMessage.isDefined =>
        rootResult.copy(message = rootResult.message + "\n" + additionalMessage.getOrElse(""))
      case rootResult :: Nil => rootResult
      case _ :: _ =>
        val severity =
          if (sortedResults.exists(_.level == SeverityLevels.VIOLATION)) SeverityLevels.VIOLATION
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
          d.annotations.find(classOf[LexicalInformation]),
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
      case obj: ObjectNode => obj.properties.values.flatMap(p => p.id +: indexSons(p)).toSeq
      case arr: ArrayNode =>
        arr.members.flatMap(m => { m.id +: indexSons(m) })
      case other => Nil // if other type, dont have childrens, and the root id is indexed at data node entry level.
    }
  }

  def aggregate(indexedResult: Map[String, Seq[AMFValidationResult]]): Seq[AMFValidationResult] =
    index.flatMap { case (id, entry) => entry aggregate indexedResult }.toSeq
}
