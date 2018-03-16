package amf.javaparser.org.raml.json_schema

import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.remote.Vendor
import amf.javaparser.org.raml.ModelValidationTest
import amf.plugins.domain.shapes.models.AnyShape

/* this test parse a raml only with declared types, resolve them and serialize a json schema.*/

/* Only validate, beacuse resolution deletes all declared types (this examples are apis with only declared types)
but resolution for validation normalize shapes.
 */

class TypeToJsonSchemaTest extends ModelValidationTest {
  override def path: String = "amf-client/shared/src/test/resources/org/raml/json_schema"

  override def inputFileName: String = "input.raml"

  override def outputFileName: String = "output.json"

  override val basePath: String = path

  override def dump(model: BaseUnit, d: String, vendor: Vendor): String = {
    model match {
      case d: DeclaresModel =>
        d.declares.collectFirst { case s: AnyShape if s.name.equals("root") => s } match {
          case Some(anyShape: AnyShape) => anyShape.toJsonSchema
          case Some(other)              => throw new AssertionError("Wrong type declared $other")
          case None                     => throw new AssertionError("Model with empty declarations")
        }
      case other => throw new AssertionError("Invalid model type $other")
    }
  }
}
