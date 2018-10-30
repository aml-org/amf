package amf.plugins.domain.shapes.validation

import amf.ProfileName
import amf.client.plugins._
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.registries.AMFPluginsRegistry
import amf.core.utils._
import amf.core.validation._
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.plugins.features.validation.ParserSideValidations

import scala.collection.mutable
import scala.concurrent.Future

object PayloadValidationPluginsHandler {

  def validateAll(candidates: Seq[ValidationCandidate],
                  severity: String,
                  env: Environment): Future[AMFValidationReport] = {
    val validators: mutable.Map[(Shape, String), PayloadValidator] = mutable.Map()

    val value: Seq[AMFValidationReport] = candidates.map { c =>
      val validator = validators.get((c.shape, c.payload.mediaType.value())) match {
        case Some(v) => v
        case _ =>
          val v: PayloadValidator = plugin(c.payload.mediaType.value(), c.shape, env, severity).validator(c.shape, env)
          validators.put((c.shape, c.payload.mediaType.value()), v)
          v
      }

      validator.validate(c.payload)
    }

    val seq = value.flatMap { report =>
      report.results.sorted
    }.toSeq
    Future.successful(AMFValidationReport(!seq.exists(_.level == SeverityLevels.VIOLATION), "", ProfileName(""), seq))
  }

  def validateFragment(shape: Shape,
                       fragment: PayloadFragment,
                       severity: String,
                       env: Environment = Environment(),
                       validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] = {
    val p = plugin(fragment.mediaType.value(), shape, env, severity)

    Future.successful(p.validator(shape, env, validationMode).validate(fragment))
  }

  def validateWithGuessing(shape: Shape,
                           payload: String,
                           severity: String,
                           env: Environment = Environment(),
                           validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] =
    validate(shape, payload.guessMediaType(isScalar = false), payload, severity, env, validationMode)

  def validate(shape: Shape,
               mediaType: String,
               payload: String,
               severity: String,
               env: Environment = Environment(),
               validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] = {
    Future.successful({
      val p = plugin(mediaType, shape, env, severity)

      p.validator(shape, env, validationMode).validate(mediaType, payload)
    })

  }

  def payloadValidator(shape: Shape,
                       mediaType: String,
                       env: Environment,
                       validationMode: ValidationMode): Option[PayloadValidator] =
    searchPlugin(mediaType, shape, env, SeverityLevels.VIOLATION).map(_.validator(shape, env, validationMode))

  private def plugin(mediaType: String,
                     shape: Shape,
                     env: Environment,
                     defaultSeverity: String): AMFPayloadValidationPlugin =
    searchPlugin(mediaType, shape, env, defaultSeverity)
      .getOrElse(AnyMatchPayloadPlugin(defaultSeverity))

  private def searchPlugin(mediaType: String,
                           shape: Shape,
                           env: Environment,
                           defaultSeverity: String): Option[AMFPayloadValidationPlugin] =
    AMFPluginsRegistry
      .dataNodeValidatorPluginForMediaType(mediaType)
      .find { plugin =>
        plugin.canValidate(shape, env)
      }

  private case class AnyMatchPayloadPlugin(defaultSeverity: String) extends AMFPayloadValidationPlugin {

    override val payloadMediaType: Seq[String] = Nil

    override def canValidate(shape: Shape, env: Environment): Boolean =
      false // this not should be indexed, its the default for not match

    override val ID: String = "Any match"

    override def dependencies(): Seq[AMFPlugin] = Nil

    override def init(): Future[AMFPlugin] = Future.successful(this)
    override def validator(s: Shape, env: Environment, validationMode: ValidationMode): PayloadValidator =
      AnyMathPayloadValidator(s, defaultSeverity)
  }

  case class AnyMathPayloadValidator(shape: Shape, defaultSeverity: String) extends PayloadValidator {
    override def validate(payload: String, mediaType: String): AMFValidationReport = {

      val results = AMFValidationResult(
        s"Unsupported validation for mediatype: $mediaType and shape ${shape.id}",
        defaultSeverity,
        "",
        Some((Namespace.Document + "value").iri()),
        if (defaultSeverity == SeverityLevels.VIOLATION)
          ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id
        else ParserSideValidations.UnsupportedExampleMediaTypeWarningSpecification.id,
        None,
        None,
        null
      )
      AMFValidationReport("", ProfileName(""), Seq(results))

    }

    override def validate(payloadFragment: PayloadFragment): AMFValidationReport = {
      val results = AMFValidationResult(
        s"Unsupported validation for mediatype: ${payloadFragment.mediaType.value()} and shape ${shape.id}",
        defaultSeverity,
        payloadFragment.encodes.id,
        Some((Namespace.Document + "value").iri()),
        if (defaultSeverity == SeverityLevels.VIOLATION)
          ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id
        else ParserSideValidations.UnsupportedExampleMediaTypeWarningSpecification.id,
        payloadFragment.encodes.position(),
        payloadFragment.encodes.location(),
        null
      )

      AMFValidationReport("", ProfileName(""), Seq(results))
    }

    override def fastValidation(mediaType: String, payload: String): Boolean = validate(mediaType, payload).conforms
    override val validationMode: ValidationMode                              = StrictValidationMode
    override val env: Environment                                            = Environment()
  }
}
