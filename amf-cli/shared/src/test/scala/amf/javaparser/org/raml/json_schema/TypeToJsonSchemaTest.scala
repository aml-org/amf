package amf.javaparser.org.raml.json_schema

import amf.apicontract.client.scala.{AMFConfiguration, WebAPIConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.internal.remote.{Hint, Oas20YamlHint, Oas30YamlHint, Vendor}
import amf.javaparser.org.raml.ModelValidationTest
import amf.shapes.client.scala.model.domain.AnyShape
import amf.testing.Target

/* this test parse a raml only with declared types, resolve them and serialize a json schema.*/

/* Only validate, because resolution deletes all declared types (this examples are apis with only declared types)
but resolution for validation normalize shapes.
 */

trait TypeToJsonSchemaTest extends ModelValidationTest {

  override val basePath: String = path

  override def render(model: BaseUnit, d: String, vendor: Hint, amfConfig: AMFConfiguration): String = {
    model match {
      case d: DeclaresModel =>
        d.declares.collectFirst { case s: AnyShape if s.name.is("root") => s } match {
          case Some(anyShape: AnyShape) => renderShape(anyShape)
          case Some(other)              => throw new AssertionError("Wrong type declared $other")
          case None                     => throw new AssertionError("Model with empty declarations")
        }
      case other => throw new AssertionError("Invalid model type $other")
    }
  }

  def renderShape(shape: AnyShape): String
}

class RamlTypeToNormalJsonSchemaTest extends TypeToJsonSchemaTest {
  override def path: String           = "amf-cli/shared/src/test/resources/org/raml/json_schema/"
  override def inputFileName: String  = "input.raml"
  override def outputFileName: String = "output.json"
  override def renderShape(shape: AnyShape): String = {
    val config = WebAPIConfiguration
      .WebAPI()
      .withRenderOptions(RenderOptions().withoutCompactedEmission)
    config.elementClient().toJsonSchema(shape)
  }
}

class RamlTypeToCompactJsonSchemaTest extends TypeToJsonSchemaTest {
  override def path: String           = "amf-cli/shared/src/test/resources/org/raml/json_schema/"
  override def inputFileName: String  = "input.raml"
  override def outputFileName: String = "compact-output.json"
  override def renderShape(shape: AnyShape): String =
    WebAPIConfiguration.WebAPI().elementClient().buildJsonSchema(shape)
}

// Uncomment to add suite

//class OasTypeToNormalJsonSchemaTest extends TypeToJsonSchemaTest {
//  override def path: String                         = "amf-cli/shared/src/test/resources/org/oas/json_schema/"
//  override def inputFileName: String                = "input.json"
//  override def outputFileName: String               = "output.json"
//  override def hint: Hint                           = OasJsonHint
//  override def renderShape(shape: AnyShape): String = shape.toJsonSchema
//}
//

trait OasTypeToCompactJsonSchemaTest extends TypeToJsonSchemaTest {
  override def inputFileName: String  = "input.json"
  override def outputFileName: String = "compact-output.json"
  override def renderShape(shape: AnyShape): String =
    WebAPIConfiguration.WebAPI().elementClient().buildJsonSchema(shape)
}

case class Oas20TypeToCompactJsonSchemaTest() extends OasTypeToCompactJsonSchemaTest {
  override def path: String = "amf-cli/shared/src/test/resources/org/oas/json_schema/oas20/"
  override def hint: Hint   = Oas20YamlHint
}

case class Oas30TypeToCompactJsonSchemaTest() extends OasTypeToCompactJsonSchemaTest {
  override def path: String = "amf-cli/shared/src/test/resources/org/oas/json_schema/oas30/"
  override def hint: Hint   = Oas30YamlHint
}
