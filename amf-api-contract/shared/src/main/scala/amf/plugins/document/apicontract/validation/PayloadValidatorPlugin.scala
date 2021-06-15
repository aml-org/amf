package amf.plugins.document.apicontract.validation

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.{AMFPayloadValidationPlugin, PayloadValidator}
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.validation.ValidationConfiguration
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.remod.ShapePayloadValidatorFactory

import scala.concurrent.{ExecutionContext, Future}

object PayloadValidatorPlugin extends AMFPayloadValidationPlugin {

  override def canValidate(shape: Shape, config: ValidationConfiguration): Boolean = {
    shape match {
      case _: SchemaShape => false
      case _: AnyShape    => true
      case _              => false
    }
  }

  override val ID: String = "AMF Payload Validation"

//  override def dependencies(): Seq[AMFPlugin] = Nil

//  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)

  override val payloadMediaType: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")

  override def validator(s: Shape, config: ValidationConfiguration, validationMode: ValidationMode): PayloadValidator =
    ShapePayloadValidatorFactory.createValidator(s, config, validationMode)
}
