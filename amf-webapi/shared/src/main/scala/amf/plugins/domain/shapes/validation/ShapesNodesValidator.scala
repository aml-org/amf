package amf.plugins.domain.shapes.validation

import amf.core.metamodel.document.PayloadFragmentModel
import amf.core.metamodel.domain.ShapeModel
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
        case s: Shape if s.values.nonEmpty => shapeEnumCandidates(s)
        case _                             => Nil
      })
    //call to validation
    PayloadValidationPluginsHandler.validateAll(enumsCandidates.toSeq, severity, env)
  }

  private[validation] def shapeEnumCandidates(shape: Shape): Seq[ValidationCandidate] = {
    val enums       = shape.values
    val shallowCopy = shape.copyShape()
    shallowCopy.fields.removeField(ShapeModel.Values) // remove enum values from shape as is in not necessary when validating each enum value.
    enums.map(v => ValidationCandidate(shallowCopy, PayloadFragment(v, defaultMediaTypeFor(v))))
  }

  def defaultMediaTypeFor(dataNode: DataNode): String = dataNode match {
    case s: ScalarNode if s.value.option().exists(_.isXml) => "application/xml"
    case other                                             => "application/json"
  }
}
