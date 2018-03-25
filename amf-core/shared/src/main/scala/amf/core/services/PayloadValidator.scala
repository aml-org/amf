package amf.core.services

import amf.core.annotations.LexicalInformation
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.plugins.AMFPayloadValidationPlugin
import amf.core.registries.AMFPluginsRegistry
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.plugins.features.validation.ParserSideValidations

import scala.concurrent.Future

object PayloadValidator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def validate(shape: Shape, payload: String, severity: String): Future[AMFValidationReport] = {

    val mediaType = guessMediaType(payload)
    plugin(mediaType, shape).fold(error(severity, shape.id, shape, mediaType, None))(
      _.validatePayload(shape, payload, mediaType))
  }

  def validate(shape: Shape, fragment: PayloadFragment, severity: String): Future[AMFValidationReport] = {
    val mediaType = fragment.mediaType.value()
    plugin(mediaType, shape)
      .fold(
        error(severity,
              fragment.encodes.id,
              shape,
              mediaType,
              fragment.encodes.annotations.find(classOf[LexicalInformation])))(_.validatePayload(shape, fragment))
  }

  def plugin(mediaType: String, shape: Shape): Option[AMFPayloadValidationPlugin] =
    AMFPluginsRegistry.dataNodeValidatorPluginForMediaType(mediaType).find { plugin =>
      plugin.canValidate(shape)
    }

  private def error(severity: String,
                    modelId: String,
                    shape: Shape,
                    mediaType: String,
                    lexical: Option[LexicalInformation] = None) = {
    Future {
      val result = AMFValidationResult(
        s"Any validator plugin matches for shape $shape and mediatype: $mediaType",
        severity,
        modelId,
        Some((Namespace.Document + "value").iri()),
        ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id(),
        lexical,
        null
      )
      AMFValidationReport(if (severity == SeverityLevels.WARNING) true else false, modelId, "Payload", Seq(result))
    }
  }

  def guessMediaType(value: String): String = {
    if (isXml(value)) "application/xml"
    else if (isJson(value)) "application/json"
    else "text/vnd.yaml" // by default, we will try to parse it as YAML
  }

  private def isXml(value: String) = value.trim.startsWith("<")

  private def isJson(value: String) = value.trim.startsWith("{") || value.startsWith("[")
}
