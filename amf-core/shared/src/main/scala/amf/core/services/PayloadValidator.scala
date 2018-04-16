package amf.core.services

import amf.core.annotations.LexicalInformation
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.plugins.{AMFPayloadValidationPlugin, AMFPlugin}
import amf.core.registries.AMFPluginsRegistry
import amf.core.validation._
import amf.core.vocabulary.Namespace
import amf.plugins.features.validation.ParserSideValidations

import scala.collection.immutable
import scala.concurrent.Future

object PayloadValidator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def validateAll(candidates: Seq[ValidationCandidate], severity: String): Future[AMFValidationReport] = {
    val pluginToCandidates: Map[AMFPayloadValidationPlugin, Seq[ValidationCandidate]] = candidates.groupBy { c =>
      plugin(c.payload.mediaType.value(), c.shape)
    }
    val value: immutable.Iterable[Future[AMFValidationReport]] = pluginToCandidates.map { candidate =>
      candidate._1.validateSet(ValidationShapeSet(candidate._2, severity))
    }
    Future
      .sequence(value)
      .map(s => {
        val seq = s.flatMap { report =>
          report.results
        }.toSeq
        AMFValidationReport(!seq.exists(_.level == SeverityLevels.VIOLATION), "", "", seq)
      })
  }

  def validate(shape: Shape, fragment: PayloadFragment, severity: String): Future[AMFValidationReport] =
    validateAll(Seq(ValidationCandidate(shape, fragment)), severity)

  def validate(shape: Shape, payload: String, severity: String): Future[AMFValidationReport] = {

    val mediaType = guessMediaType(isScalar = false, payload)
    plugin(mediaType, shape).validatePayload(shape, payload, mediaType)
  }

  def plugin(mediaType: String, shape: Shape): AMFPayloadValidationPlugin =
    AMFPluginsRegistry
      .dataNodeValidatorPluginForMediaType(mediaType)
      .find { plugin =>
        plugin.canValidate(shape)
      }
      .getOrElse(AnyMatchPayloadPlugin)

  def guessMediaType(isScalar: Boolean, value: String): String = {
    if (isXml(value) && !isScalar) "application/xml"
    else if (isJson(value) && !isScalar) "application/json"
    else "text/vnd.yaml" // by default, we will try to parse it as YAML
  }

  def isXml(value: String): Boolean = value.trim.startsWith("<")

  def isJson(value: String) = value.trim.startsWith("{") || value.startsWith("[")

  private object AnyMatchPayloadPlugin extends AMFPayloadValidationPlugin {

    override val payloadMediaType: Seq[String] = Nil

    override def canValidate(shape: Shape): Boolean =
      false // this not should be indexed, its the default for not match

    override val ID: String = "Any match"

    override def dependencies(): Seq[AMFPlugin] = Nil

    override def init(): Future[AMFPlugin] = Future.successful(this)

    override def validateSet(set: ValidationShapeSet): Future[AMFValidationReport] = Future {
      val results = set.candidates.map { c =>
        AMFValidationResult(
          s"Unsupported validation for mediatype: ${c.payload.mediaType} and shape ${c.shape.id}",
          set.defaultSeverity,
          c.payload.encodes.id,
          Some((Namespace.Document + "value").iri()),
          if (set.defaultSeverity == SeverityLevels.VIOLATION)
            ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id()
          else ParserSideValidations.UnsupportedExampleMediaTypeWarningSpecification.id(),
          c.payload.encodes.annotations.find(classOf[LexicalInformation]),
          null
        )
      }
      AMFValidationReport(if (set.defaultSeverity == SeverityLevels.WARNING) true else false, "", "Payload", results)
    }

    override protected def parsePayload(payload: String, mediaType: String): PayloadFragment =
      PayloadFragment(payload, mediaType)
  }
}
