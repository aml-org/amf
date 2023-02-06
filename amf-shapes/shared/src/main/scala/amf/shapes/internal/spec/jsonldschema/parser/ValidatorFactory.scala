package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.common.validation.ScalarRelaxedValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.AMFShapePayloadValidator
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.plugin.FailFastJsonSchemaPayloadValidationPlugin

trait ValidatorFactory {

  def fullValidator(shape: Shape): AMFShapePayloadValidator
  def failFastValidator(shape: Shape): AMFShapePayloadValidator
}

object ConfigValidatorFactory extends ValidatorFactory {

  private val fullClient = ShapesConfiguration
    .predefined()
    .elementClient()

  private val fastClient = ShapesConfiguration
    .predefined()
    .withPlugin(FailFastJsonSchemaPayloadValidationPlugin)
    .elementClient()

  private val mediaType = Mimes.`application/json`

  private val mode: ScalarRelaxedValidationMode.type = ScalarRelaxedValidationMode

  override def fullValidator(shape: Shape): AMFShapePayloadValidator =
    fullClient.payloadValidatorFor(shape, mediaType, mode)

  override def failFastValidator(shape: Shape): AMFShapePayloadValidator =
    fastClient.payloadValidatorFor(shape, mediaType, mode)
}
