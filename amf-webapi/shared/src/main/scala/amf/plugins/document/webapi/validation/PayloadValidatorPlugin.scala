package amf.plugins.document.webapi.validation

import amf.client.parse.DefaultParserErrorHandler
import amf.client.plugins._
import amf.core.model.domain._
import amf.core.parser.ParserContext
import amf.core.validation._
import amf.internal.environment.Environment
import amf.plugins.document.webapi.contexts.parser.raml.PayloadContext
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.plugins.domain.webapi.unsafe.JsonSchemaSecrets

import scala.concurrent.{ExecutionContext, Future}

object PayloadValidatorPlugin extends AMFPayloadValidationPlugin with JsonSchemaSecrets {

  override def canValidate(shape: Shape, env: Environment): Boolean = {
    shape match {
      case _: SchemaShape => false
      case _: AnyShape    => true
      case _              => false
    }
  }

  override val ID: String = "AMF Payload Validation"

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init()(implicit executionContext: ExecutionContext): AMFPlugin = this

  override val payloadMediaType: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")

  val defaultCtx = new PayloadContext("", Nil, ParserContext(eh = DefaultParserErrorHandler.withRun()))

  override def validator(s: Shape, env: Environment, validationMode: ValidationMode): PayloadValidator =
    payloadValidator(s, env, validationMode)
}
