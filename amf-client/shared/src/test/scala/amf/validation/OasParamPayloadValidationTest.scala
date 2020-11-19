package amf.validation

import amf.client.plugins.{ScalarRelaxedValidationMode, ValidationMode}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.Shape
import amf.core.remote.{Hint, OasJsonHint}
import amf.plugins.domain.webapi.models.api.WebApi

class OasParamPayloadValidationTest extends ApiShapePayloadValidationTest {

  override val basePath = "file://amf-client/shared/src/test/resources/validations/param-payload/"
  protected def fixtureList: Seq[Fixture] = Seq(
    Fixture("param validation", "oas_data.json", "2015-07-20T21:00:00", conforms = true),
    Fixture("param validation quoted number in string", "oas_data.json", "\"2\"", conforms = true),
    Fixture("param validation quoted boolean in string", "oas_data.json", "\"true\"", conforms = true),
    Fixture("param validation boolean in string", "oas_data.json", "true", conforms = true),
    Fixture("param validation number against string", "oas_data.json", "2", conforms = true)
  )

  override protected def findShape(d: Document): Shape =
    d.encodes
      .asInstanceOf[WebApi]
      .endPoints
      .head
      .operations
      .head
      .request
      .headers
      .head
      .schema

  override protected val hint: Hint = OasJsonHint

  override def transform(unit: BaseUnit): BaseUnit = unit

  override def validationMode: ValidationMode = ScalarRelaxedValidationMode
}
