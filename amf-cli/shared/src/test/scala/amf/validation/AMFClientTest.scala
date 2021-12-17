package amf.validation

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.ExternalSourceElement
import amf.shapes.client.scala.model.domain.{NodeShape, SchemaShape}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class AMFClientTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath                      = "file://amf-cli/shared/src/test/resources/validations"
  val ro: RenderOptions             = RenderOptions().withCompactUris.withPrettyPrint.withSourceMaps
  val ramlConfig: AMFConfiguration  = RAMLConfiguration.RAML10().withRenderOptions(ro)
  val ramlClient: AMFBaseUnitClient = ramlConfig.baseUnitClient()
  val oasConfig: AMFConfiguration   = OASConfiguration.OAS30().withRenderOptions(ro)
  val oasClient: AMFBaseUnitClient  = oasConfig.baseUnitClient()
  val oas2Config: AMFConfiguration  = OASConfiguration.OAS20().withRenderOptions(ro)
  val oas2Client: AMFBaseUnitClient = oas2Config.baseUnitClient()

  test("AMF should persist and restore the raw XML schema") {
    val api = s"$basePath/raml/raml-with-xml/api.raml"

    ramlClient.parse(api) flatMap { parseResult =>
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
      val transformResult = ramlClient.transform(parseResult.baseUnit, PipelineId.Editing)
      val rendered        = ramlClient.render(transformResult.baseUnit, "application/ld+json")

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
    val api = s"$basePath/raml/raml-with-json-schema/api.raml"

    ramlClient.parse(api) flatMap { parseResult =>
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
      val transformResult = ramlClient.transform(parseResult.baseUnit, PipelineId.Editing)
      val rendered        = ramlClient.render(transformResult.baseUnit, "application/ld+json")

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

  // github issue #1086
  test("AMF should not remove well known annotations") {
    val ramlApi = s"$basePath/raml/api-with-annotations.raml"

    def getEndpointAnnotations(bu: BaseUnit) =
      bu.asInstanceOf[Document]
        .encodes
        .asInstanceOf[WebApi]
        .endPoints
        .head
        .operations
        .head
        .customDomainProperties

    ramlClient.parse(ramlApi) flatMap { parseResult =>
      val transformResult = ramlClient.transform(parseResult.baseUnit, PipelineId.Default)
      val ramlAnnotations = getEndpointAnnotations(transformResult.baseUnit)
      ramlAnnotations.length shouldBe 2

      val oasTransformResult = oasClient.transform(parseResult.baseUnit, PipelineId.Compatibility)
      val oasAnnotations     = getEndpointAnnotations(oasTransformResult.baseUnit)
      oasAnnotations.length shouldBe 2
    }
  }
}
