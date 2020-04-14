package amf.plugins.domain.shapes.validation

import amf.core.metamodel.document.PayloadFragmentModel
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DataNode, ScalarNode, Shape}
import amf.core.utils.MediaTypeMatcher
import amf.core.validation.{AMFValidationReport, ValidationCandidate}
import amf.internal.environment.Environment

import scala.concurrent.{ExecutionContext, Future}
object ShapesNodesValidator {

  def validateAll(candidates: Seq[ValidationCandidate], severity: String, env: Environment)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    validateEnums(candidates, severity, env).flatMap { r =>
      if (!r.conforms) Future.successful(r)
      else
        // filter for only enum cases
        PayloadValidationPluginsHandler.validateAll(
          candidates.filter(_.payload.fields.exists(PayloadFragmentModel.Encodes)),
          severity,
          env) // filter for only enum cases
    }
  }

  private def validateEnums(validationCandidates: Seq[ValidationCandidate], severity: String, env: Environment)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val enumsCandidates = validationCandidates
      .groupBy(_.shape)
      .keys
      .flatMap({
        case s: Shape if s.values.nonEmpty =>
          s.values.map(v => ValidationCandidate(s, PayloadFragment(v, defaultMediaTypeFor(v))))
        case _ => Nil
      })
    //call to validation
    PayloadValidationPluginsHandler.validateAll(enumsCandidates.toSeq, severity, env)
  }

  def defaultMediaTypeFor(dataNode: DataNode): String = dataNode match {
    case s: ScalarNode if s.value.option().exists(_.isXml) => "application/xml"
    case other                                             => "application/json"
  }
}
