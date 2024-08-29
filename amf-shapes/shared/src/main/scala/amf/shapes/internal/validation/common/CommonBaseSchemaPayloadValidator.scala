package amf.shapes.internal.validation.common

import amf.core.client.common.validation.ProfileNames.AMF
import amf.core.client.common.validation.{ProfileName, SeverityLevels, ValidationMode}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, ScalarNode}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.client.scala.validation.payload.AMFShapePayloadValidator
import amf.core.internal.plugins.syntax.SyamlSyntaxRenderPlugin
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.internal.spec.common.emitter.PayloadEmitter
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification

import java.io.StringWriter
import scala.collection.mutable

abstract class CommonBaseSchemaPayloadValidator extends AMFShapePayloadValidator {

  protected val supportedMediaTypes: Seq[String]

  protected val defaultSeverity: String = SeverityLevels.VIOLATION
  protected def getReportProcessor(profileName: ProfileName): ValidationProcessor
  protected def getReportProcessor: ValidationProcessor = getReportProcessor(AMF)

  protected def isValidMediaType(mediaType: String): Boolean = supportedMediaTypes.contains(mediaType)
  protected def mediaTypeError(mediaType: String): AMFValidationResult = AMFValidationResult(
    s"Unsupported payload media type '$mediaType', only ${supportedMediaTypes.toString()} supported",
    SeverityLevels.VIOLATION,
    "",
    None,
    ExampleValidationErrorSpecification.id,
    None,
    None,
    null
  )

  protected type LoadedObj
  protected type LoadedSchema
  protected val validationMode: ValidationMode

  protected val schemas: mutable.Map[String, LoadedSchema] = mutable.Map()

  protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj]

  protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]]

  protected def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some("") => None
      case _ =>
        val document = PayloadEmitter(payload.encodes)(UnhandledErrorHandler).emitDocument()
        val writer   = new StringWriter()
        SyamlSyntaxRenderPlugin.emit(`application/json`, SyamlParsedDocument(document), writer).map(_.toString)
    }

    futureText map { text =>
      payload.encodes match {
        case node: ScalarNode
            if node.dataType
              .option()
              .contains(DataType.String) && text.nonEmpty && text.head != '"' =>
          "\"" + text.stripLineEnd + "\""
        case _ => text.stripLineEnd
      }
    }
  }
}
