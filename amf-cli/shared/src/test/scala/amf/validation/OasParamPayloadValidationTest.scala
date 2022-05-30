package amf.validation

import amf.apicontract.client.scala.AMFBaseUnitClient
import amf.core.client.common.validation.{ScalarRelaxedValidationMode, ValidationMode}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.remote.Oas20JsonHint
import amf.apicontract.client.scala.model.domain.api.WebApi

class OasParamPayloadValidationTest extends ApiShapePayloadValidationTest {

  override val basePath = "file://amf-cli/shared/src/test/resources/validations/param-payload/"
  protected def fixtureList: Seq[Fixture] = Seq(
    Fixture("param validation", "oas_data.json", "2015-07-20T21:00:00", conforms = true, hint = Oas20JsonHint),
    Fixture(
      "param validation quoted number in string",
      "oas_data.json",
      "\"2\"",
      conforms = true,
      hint = Oas20JsonHint
    ),
    Fixture(
      "param validation quoted boolean in string",
      "oas_data.json",
      "\"true\"",
      conforms = true,
      hint = Oas20JsonHint
    ),
    Fixture("param validation boolean in string", "oas_data.json", "true", conforms = true, hint = Oas20JsonHint),
    Fixture("param validation number against string", "oas_data.json", "2", conforms = true, hint = Oas20JsonHint)
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

  override def transform(unit: BaseUnit, client: AMFBaseUnitClient): BaseUnit = unit

  override def validationMode: ValidationMode = ScalarRelaxedValidationMode
}
