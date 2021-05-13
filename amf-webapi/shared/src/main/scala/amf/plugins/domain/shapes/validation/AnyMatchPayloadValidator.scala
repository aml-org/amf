package amf.plugins.domain.shapes.validation

import amf.ProfileName
import amf.client.plugins.{AMFPlugin, StrictValidationMode, ValidationMode}
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.{
  AMFPayloadValidationPlugin,
  AMFValidationReport,
  AMFValidationResult,
  PayloadValidator,
  SeverityLevels
}
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.validations.ParserSideValidations
import amf.validations.PayloadValidations.UnsupportedExampleMediaTypeWarningSpecification

import scala.concurrent.{ExecutionContext, Future}

private case class AnyMatchPayloadPlugin(defaultSeverity: String) extends AMFPayloadValidationPlugin {

  override val payloadMediaType: Seq[String] = Nil

  override def canValidate(shape: Shape, env: Environment): Boolean =
    false // this not should be indexed, its the default for not match

  override val ID: String = "Any match"

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)
  override def validator(s: Shape, env: Environment, validationMode: ValidationMode): PayloadValidator =
    AnyMathPayloadValidator(s, defaultSeverity)
}

case class AnyMathPayloadValidator(shape: Shape, defaultSeverity: String) extends PayloadValidator {
  override def validate(payload: String, mediaType: String)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    val results = AMFValidationResult(
      s"Unsupported validation for mediatype: $mediaType and shape ${shape.id}",
      defaultSeverity,
      "",
      Some((Namespace.Document + "value").iri()),
      if (defaultSeverity == SeverityLevels.VIOLATION)
        ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id
      else UnsupportedExampleMediaTypeWarningSpecification.id,
      None,
      None,
      null
    )
    Future(AMFValidationReport("", ProfileName(""), Seq(results)))
  }

  override def validate(payloadFragment: PayloadFragment)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val results = AMFValidationResult(
      s"Unsupported validation for mediatype: ${payloadFragment.mediaType.value()} and shape ${shape.id}",
      defaultSeverity,
      payloadFragment.encodes.id,
      Some((Namespace.Document + "value").iri()),
      if (defaultSeverity == SeverityLevels.VIOLATION)
        ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id
      else UnsupportedExampleMediaTypeWarningSpecification.id,
      payloadFragment.encodes.position(),
      payloadFragment.encodes.location(),
      null
    )
    Future(AMFValidationReport("", ProfileName(""), Seq(results)))
  }
  override def syncValidate(mediaType: String, payload: String): AMFValidationReport =
    AMFValidationReport("", ProfileName(""), Seq())
  override def isValid(mediaType: String, payload: String)(
      implicit executionContext: ExecutionContext): Future[Boolean] =
    validate(mediaType, payload).map(_.conforms)
  override val validationMode: ValidationMode = StrictValidationMode
  override val env: Environment               = Environment()
}
