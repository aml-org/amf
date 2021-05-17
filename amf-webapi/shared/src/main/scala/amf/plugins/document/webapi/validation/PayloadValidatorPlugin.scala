package amf.plugins.document.webapi.validation

import amf.client.plugins._
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.domain._
import amf.core.validation._
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

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)

  override val payloadMediaType: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")

  override def validator(s: Shape, config: ValidationConfiguration, validationMode: ValidationMode): PayloadValidator =
    ShapePayloadValidatorFactory.createValidator(s, config, validationMode)
}
