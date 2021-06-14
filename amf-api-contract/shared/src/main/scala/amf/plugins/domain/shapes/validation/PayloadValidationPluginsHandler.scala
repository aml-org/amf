package amf.plugins.domain.shapes.validation

import amf.core.client.common.validation.{ProfileName, SeverityLevels, StrictValidationMode, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.{AMFPayloadValidationPlugin, PayloadValidator}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.registries.domain.AMFPluginsRegistry
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils._
import amf.core.internal.validation.{ValidationCandidate, ValidationConfiguration}
import amf.validations.ParserSideValidations
import amf.validations.PayloadValidations.UnsupportedExampleMediaTypeWarningSpecification

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object PayloadValidationPluginsHandler extends PlatformSecrets {

  def validateAll(candidates: Seq[ValidationCandidate], severity: String, config: ValidationConfiguration)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val validators: mutable.Map[(Shape, String), PayloadValidator] = mutable.Map()

    val futures: Seq[Future[AMFValidationReport]] = candidates.map { c =>
      val validator = validators.get((c.shape, c.payload.mediaType.value())) match {
        case Some(v) => v
        case _ =>
          val v: PayloadValidator = plugin(c.payload.mediaType.value(),
                                           c.shape,
                                           config: ValidationConfiguration,
                                           severity).validator(c.shape, config)
          validators.put((c.shape, c.payload.mediaType.value()), v)
          v
      }
      validator.validate(c.payload)
    }

    Future.sequence(futures).map { f =>
      val seq = f.flatMap { report =>
        report.results.sorted
      }
      AMFValidationReport("", ProfileName(""), seq)
    }
  }

  def validateFragment(shape: Shape,
                       fragment: PayloadFragment,
                       severity: String,
                       config: ValidationConfiguration,
                       validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = config.executionContext
    val p                                           = plugin(fragment.mediaType.value(), shape, config, severity)

    p.validator(shape, config, validationMode).validate(fragment)
  }

  def validateWithGuessing(shape: Shape,
                           payload: String,
                           severity: String,
                           config: ValidationConfiguration,
                           validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] =
    validate(shape, payload.guessMediaType(isScalar = false), payload, severity, config, validationMode)

  def validate(shape: Shape,
               mediaType: String,
               payload: String,
               severity: String,
               config: ValidationConfiguration,
               validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = config.executionContext
    val p                                           = plugin(mediaType, shape, config, severity)

    p.validator(shape, config, validationMode).validate(mediaType, payload)
  }

  def payloadValidator(shape: Shape,
                       mediaType: String,
                       config: ValidationConfiguration,
                       validationMode: ValidationMode): Option[PayloadValidator] =
    searchPlugin(mediaType, shape, config).map(_.validator(shape, config, validationMode))

  private def plugin(mediaType: String,
                     shape: Shape,
                     config: ValidationConfiguration,
                     defaultSeverity: String): AMFPayloadValidationPlugin =
    searchPlugin(mediaType, shape, config)
      .getOrElse(AnyMatchPayloadPlugin(defaultSeverity))

  private def searchPlugin(mediaType: String,
                           shape: Shape,
                           config: ValidationConfiguration): Option[AMFPayloadValidationPlugin] =
    AMFPluginsRegistry
      .dataNodeValidatorPluginForMediaType(mediaType)
      .find(_.canValidate(shape, config: ValidationConfiguration))

  private case class AnyMatchPayloadPlugin(defaultSeverity: String) extends AMFPayloadValidationPlugin {

    override val payloadMediaType: Seq[String] = Nil

    override def canValidate(shape: Shape, config: ValidationConfiguration): Boolean =
      false // this not should be indexed, its the default for not match

    override val ID: String = "Any match"

    override def dependencies(): Seq[AMFPlugin] = Nil

    override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)
    override def validator(s: Shape,
                           config: ValidationConfiguration,
                           validationMode: ValidationMode): PayloadValidator =
      AnyMathPayloadValidator(s, defaultSeverity, config)
  }

  case class AnyMathPayloadValidator(shape: Shape,
                                     defaultSeverity: String,
                                     override val configuration: ValidationConfiguration)
      extends PayloadValidator {
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
  }
}
