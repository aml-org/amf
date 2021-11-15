package amf.validation

import amf.apicontract.client.scala.RAMLConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.domain.ExternalSourceElement
import amf.shapes.client.scala.model.domain.{NodeShape, SchemaShape}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class AMFClientTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("AMF should persist and restore the raw XML schema") {
    val api           = "file://amf-cli/shared/src/test/resources/validations/raml/raml-with-xml/api.raml"
    val ro            = RenderOptions().withCompactUris.withPrettyPrint.withSourceMaps
    val configuration = RAMLConfiguration.RAML10().withRenderOptions(ro)
    val client        = configuration.baseUnitClient()

    client.parse(api) flatMap { parseResult =>
      // parsing
      parseResult.conforms shouldBe true
      val schemaShapeType = "http://a.ml/vocabularies/shapes#SchemaShape"
      val xmlType         = "XmlRefSchema"
      val bu              = parseResult.baseUnit
      val types = bu.findByType(schemaShapeType) map { element =>
        element.asInstanceOf[SchemaShape]
      }
      types.length shouldBe 1
      val xml = types.find(_.name.value() == xmlType)
      xml.isDefined shouldBe true
      val raw = xml.get.raw.value()
      raw.isInstanceOf[String] shouldBe true
      raw.isEmpty shouldBe false

      // serialization
      val transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
      val rendered        = client.render(transformResult.baseUnit, "application/ld+json")

      // restoring
      val client2 = RAMLConfiguration.RAML10().baseUnitClient()
      client2.parseContent(rendered) flatMap { parseResult =>
        val restoredBu = parseResult.baseUnit
        val restoredTypes = restoredBu.findByType(schemaShapeType) map { element =>
          element.asInstanceOf[SchemaShape]
        }
        restoredTypes.length shouldBe 1
        val restoredXml = restoredTypes.find(_.name.value() == xmlType)
        restoredXml.isDefined shouldBe true
        val restoredRaw = restoredXml.get.raw.value()
        restoredRaw.isInstanceOf[String] shouldBe true
        restoredRaw.isEmpty shouldBe false
      }
    }
  }

  test("AMF should persist and restore the raw json schema") {
    val api           = "file://amf-cli/shared/src/test/resources/validations/raml/raml-with-json-schema/api.raml"
    val ro            = RenderOptions().withCompactUris.withPrettyPrint.withSourceMaps
    val configuration = RAMLConfiguration.RAML10().withRenderOptions(ro)
    val client        = configuration.baseUnitClient()

    client.parse(api) flatMap { parseResult =>
      // parsing
      parseResult.conforms shouldBe true
      val NodeShapeType = "http://www.w3.org/ns/shacl#NodeShape"
      val typeName      = "JsonSchema"
      val bu            = parseResult.baseUnit
      val nodeShapes = bu.findByType(NodeShapeType) map { element =>
        element.asInstanceOf[NodeShape]
      }
      nodeShapes.length shouldBe 3
      val definedType = nodeShapes.find(_.name.value() == typeName)
      val jsonSchema  = definedType.get.inherits.head
      // this jsonSchema node doesn't have the raw field, only the reference-id to it, and the raw value in the parsed-json-schema annotation.
      val idToRaw = jsonSchema.asInstanceOf[ExternalSourceElement].referenceId
      idToRaw.value().isEmpty shouldBe false

      // serialization
      val transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
      val rendered        = client.render(transformResult.baseUnit, "application/ld+json")

      // restoring
      val client2 = RAMLConfiguration.RAML10().baseUnitClient()
      client2.parseContent(rendered) flatMap { parseResult =>
        val restoredBu = parseResult.baseUnit
        val restoredNodeShapes = restoredBu.findByType(NodeShapeType) map { element =>
          element.asInstanceOf[NodeShape]
        }
        restoredNodeShapes.length shouldBe 2
        val restoredType       = restoredNodeShapes.find(_.name.value() == typeName)
        val restoredJsonSchema = restoredType.get
        val restoredRaw        = restoredJsonSchema.raw.value()
        restoredRaw.isInstanceOf[String] shouldBe true
        restoredRaw.isEmpty shouldBe false
      }
    }
  }
}
