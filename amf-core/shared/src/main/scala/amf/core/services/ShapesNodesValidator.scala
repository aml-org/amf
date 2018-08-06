package amf.core.services

import amf.core.metamodel.document.PayloadFragmentModel
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DataNode, ScalarNode, Shape}
import amf.core.parser.Value
import amf.core.utils.MediaTypeMatcher
import amf.core.validation.{AMFValidationReport, ValidationCandidate}
import amf.internal.environment.Environment

import scala.collection.mutable
import scala.concurrent.Future
object ShapesNodesValidator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def validateAll(candidates: Seq[ValidationCandidate],
                  severity: String,
                  env: Environment): Future[AMFValidationReport] = {

    validateEnums(candidates, severity, env).flatMap { r =>
      if (!r.conforms) Future.successful(r)
      else
        PayloadValidator.validateAll(candidates.filter(_.payload.fields.exists(PayloadFragmentModel.Encodes)),
                                     severity,
                                     env) // filter for only enum cases
    }
  }

  private def validateEnums(validationCandidates: Seq[ValidationCandidate], severity: String, env: Environment) = {
    val bkpMap: mutable.Map[Shape, Value] = mutable.Map()
    val enumsCandidates = validationCandidates
      .groupBy(_.shape)
      .keys
      .flatMap({
        case s: Shape if s.values.nonEmpty =>
          val value: Value = s.fields.entry(ShapeModel.Values).get.value
          bkpMap.put(s, value)
          val cs = s.values.map(v => ValidationCandidate(s, PayloadFragment(v, defaultMediaTypeFor(v))))
          s.fields.removeField(ShapeModel.Values)
          cs
        case _ => Nil
      })
    //call to validation
    PayloadValidator.validateAll(enumsCandidates.toSeq, severity, env).map { r =>
      restoreEnums(bkpMap)
      r
    }
  }

  private def restoreEnums(bkpMap: mutable.Map[Shape, Value]): Unit = {
    bkpMap.keys.foreach { s =>
      val e = bkpMap(s)
      s.set(ShapeModel.Values, e.value, e.annotations)
    }
  }

  def defaultMediaTypeFor(dataNode: DataNode): String = dataNode match {
    case s: ScalarNode if s.value.isXml => "application/xml"
    case other                          => "application/json"
  }
}
