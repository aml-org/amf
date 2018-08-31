package amf.core.services

import amf.{ProfileName, ProfileNames}
import amf.client.plugins._
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{ScalarNode, Shape}
import amf.core.registries.AMFPluginsRegistry
import amf.core.utils._
import amf.core.validation._
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.plugins.features.validation.ParserSideValidations

import scala.collection.immutable
import scala.concurrent.Future

object PayloadValidator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def validateAll(candidates: Seq[ValidationCandidate],
                  severity: String,
                  env: Environment): Future[AMFValidationReport] = {
    val pluginToCandidates: Map[AMFPayloadValidationPlugin, Seq[ValidationCandidate]] = candidates.groupBy { c =>
      plugin(c.payload.mediaType.value(), c.shape, env)
    }
    val value: immutable.Iterable[Future[AMFValidationReport]] = pluginToCandidates.map { candidate =>
      candidate._1.validateSet(ValidationShapeSet(candidate._2, severity), env)
    }
    Future
      .sequence(value)
      .map(s => {
        val seq = s.flatMap { report =>
          report.results.sorted
        }.toSeq
        AMFValidationReport(!seq.exists(_.level == SeverityLevels.VIOLATION), "", ProfileName(""), seq)
      })
  }

  def validate(shape: Shape, fragment: PayloadFragment, severity: String): Future[AMFValidationReport] =
    validate(shape, fragment, severity, Environment())

  def validate(shape: Shape,
               fragment: PayloadFragment,
               severity: String,
               env: Environment): Future[AMFValidationReport] =
    validateAll(Seq(ValidationCandidate(shape, fragment)), severity, env)

  def validate(shape: Shape,
               payload: String,
               severity: String,
               env: Environment = Environment(),
               validationMode: ValidationMode = StrictValidationMode): Future[AMFValidationReport] = {

    val mediaType                    = payload.guessMediaType(isScalar = false)
    val p                            = plugin(mediaType, shape, env)
    val result: PayloadParsingResult = p.parsePayloadWithErrorHandler(payload, mediaType, env, shape)
    if (result.hasError)
      Future.successful(AMFValidationReport(conforms = false, payload, ProfileNames.AMF, result.results))
    else {
      val f = if (isString(shape) && validationMode == ScalarRelaxedValidationMode) {
        result.fragment.encodes match {
          case s: ScalarNode if !s.dataType.getOrElse("").equals((Namespace.Xsd + "string").iri()) =>
            PayloadFragment(ScalarNode(s.value, Some((Namespace.Xsd + "string").iri()), s.annotations),
                            result.fragment.mediaType.value())
          case other => result.fragment
        }
      } else
        result.fragment
      p.validateSet(ValidationShapeSet(Seq(ValidationCandidate(shape, f))), env)
    }
  }

  private def isString(shape: Shape): Boolean =
    shape.ramlSyntaxKey.equals("stringScalarShape") // todo: temp solution to know if a string scalar. We need to do something with the modules. I need to see any shape hierarchy from validation.

  def plugin(mediaType: String, shape: Shape, env: Environment): AMFPayloadValidationPlugin =
    AMFPluginsRegistry
      .dataNodeValidatorPluginForMediaType(mediaType)
      .find { plugin =>
        plugin.canValidate(shape, env)
      }
      .getOrElse(AnyMatchPayloadPlugin)

  private object AnyMatchPayloadPlugin extends AMFPayloadValidationPlugin {

    override val payloadMediaType: Seq[String] = Nil

    override def canValidate(shape: Shape, env: Environment): Boolean =
      false // this not should be indexed, its the default for not match

    override val ID: String = "Any match"

    override def dependencies(): Seq[AMFPlugin] = Nil

    override def init(): Future[AMFPlugin] = Future.successful(this)

    override def validateSet(set: ValidationShapeSet, env: Environment): Future[AMFValidationReport] = Future {
      val results = set.candidates.map { c =>
        val e = c.payload.encodes
        AMFValidationResult(
          s"Unsupported validation for mediatype: ${c.payload.mediaType} and shape ${c.shape.id}",
          set.defaultSeverity,
          e.id,
          Some((Namespace.Document + "value").iri()),
          if (set.defaultSeverity == SeverityLevels.VIOLATION)
            ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id
          else ParserSideValidations.UnsupportedExampleMediaTypeWarningSpecification.id,
          e.position(),
          e.location(),
          null
        )
      }
      AMFValidationReport(if (set.defaultSeverity == SeverityLevels.WARNING) true else false,
                          "",
                          ProfileName("Payload"),
                          results)
    }

    override protected def parsePayload(payload: String,
                                        mediaType: String,
                                        env: Environment,
                                        shape: Shape): PayloadFragment =
      PayloadFragment(payload, mediaType)

    override def parsePayloadWithErrorHandler(payload: String,
                                              mediaType: String,
                                              env: Environment,
                                              shape: Shape): PayloadParsingResult =
      PayloadParsingResult(PayloadFragment(payload, mediaType), Nil)
  }
}

trait ValidationMode

object StrictValidationMode extends ValidationMode

object ScalarRelaxedValidationMode extends ValidationMode
