package amf.javaparser.org.raml.json_schema

import amf.client.environment.WebAPIConfiguration
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.remote.{Hint, Oas20YamlHint, Oas30YamlHint, Vendor}
import amf.javaparser.org.raml.ModelValidationTest
import amf.plugins.domain.shapes.models.AnyShape
import amf.remod.JsonSchemaShapeSerializer.{buildJsonSchema, toJsonSchema}

import scala.concurrent.Future

/* this test parse a raml only with declared types, resolve them and serialize a json schema.*/

/* Only validate, because resolution deletes all declared types (this examples are apis with only declared types)
but resolution for validation normalize shapes.
 */

trait TypeToJsonSchemaTest extends ModelValidationTest {

  override val basePath: String = path

  override def render(model: BaseUnit, d: String, vendor: Vendor): Future[String] = {
    model match {
      case d: DeclaresModel =>
        d.declares.collectFirst { case s: AnyShape if s.name.is("root") => s } match {
          case Some(anyShape: AnyShape) => Future { renderShape(anyShape) }
          case Some(other)              => throw new AssertionError("Wrong type declared $other")
          case None                     => throw new AssertionError("Model with empty declarations")
        }
      case other => throw new AssertionError("Invalid model type $other")
    }
  }

  def renderShape(shape: AnyShape): String
}

class RamlTypeToNormalJsonSchemaTest extends TypeToJsonSchemaTest {
  override def path: String                         = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  override def inputFileName: String                = "input.raml"
  override def outputFileName: String               = "output.json"
  override def renderShape(shape: AnyShape): String = toJsonSchema(shape)
}

class RamlTypeToCompactJsonSchemaTest extends TypeToJsonSchemaTest {
  override def path: String           = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  override def inputFileName: String  = "input.raml"
  override def outputFileName: String = "compact-output.json"
  override def renderShape(shape: AnyShape): String =
    buildJsonSchema(shape, WebAPIConfiguration.WebAPI().withRenderOptions(RenderOptions().withCompactedEmission))
}

// Uncomment to add suite

//class OasTypeToNormalJsonSchemaTest extends TypeToJsonSchemaTest {
//  override def path: String                         = "amf-client/shared/src/test/resources/org/oas/json_schema/"
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
    buildJsonSchema(shape, WebAPIConfiguration.WebAPI().withRenderOptions(RenderOptions().withCompactedEmission))
}

case class Oas20TypeToCompactJsonSchemaTest() extends OasTypeToCompactJsonSchemaTest {
  override def path: String = "amf-client/shared/src/test/resources/org/oas/json_schema/oas20/"
  override def hint: Hint   = Oas20YamlHint
}

case class Oas30TypeToCompactJsonSchemaTest() extends OasTypeToCompactJsonSchemaTest {
  override def path: String = "amf-client/shared/src/test/resources/org/oas/json_schema/oas30/"
  override def hint: Hint   = Oas30YamlHint
}
