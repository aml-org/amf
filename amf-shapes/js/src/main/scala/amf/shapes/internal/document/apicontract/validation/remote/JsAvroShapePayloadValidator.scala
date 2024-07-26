package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.{ProfileName, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.shapes.internal.validation.avro.BaseAvroSchemaPayloadValidator
import amf.shapes.internal.validation.common.ValidationProcessor

import scala.scalajs.js
import scala.scalajs.js.{Dictionary, JavaScriptException, SyntaxError}

class JsAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration,
    private val shouldFailFast: Boolean = false
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration, shouldFailFast) {

  // TODO HERE YOU HAVE TO MAKE THE CALLS TO THE VALIDATOR JS LIBRARY. CHECK JsShapePayloadValidator FOR REFERENCE

  override protected def loadAvro(text: String): LoadedObj = ???

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor = ???

  override protected type LoadedObj    = this.type
  override protected type LoadedSchema = this.type

  override protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = ???

  override protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedSchema] = ???

  override protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = ???
}
