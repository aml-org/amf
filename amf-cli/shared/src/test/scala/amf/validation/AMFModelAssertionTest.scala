package amf.validation

import amf.apicontract.client.scala._
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.ExternalSourceElement
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, SchemaShape}
import amf.testing.ConfigProvider.configFor
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class AMFModelAssertionTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath                        = "file://amf-cli/shared/src/test/resources/validations"
  val ro: RenderOptions               = RenderOptions().withCompactUris.withPrettyPrint.withSourceMaps
  val ramlConfig: AMFConfiguration    = RAMLConfiguration.RAML10().withRenderOptions(ro)
  val ramlClient: AMFBaseUnitClient   = ramlConfig.baseUnitClient()
  val raml08Config: AMFConfiguration  = RAMLConfiguration.RAML08().withRenderOptions(ro)
  val raml08Client: AMFBaseUnitClient = raml08Config.baseUnitClient()
  val oasConfig: AMFConfiguration     = OASConfiguration.OAS30().withRenderOptions(ro)
  val oasClient: AMFBaseUnitClient    = oasConfig.baseUnitClient()

  def modelAssertion(path: String, pipelineId: String = PipelineId.Default, parseOnly: Boolean = false)(
      assertion: BaseUnit => Assertion): Future[Assertion] = {
    val parser = APIConfiguration.API().baseUnitClient()
    parser.parse(path) flatMap { parseResult =>
      if (parseOnly) assertion(parseResult.baseUnit)
      else {
        val specificClient  = configFor(parseResult.sourceSpec).baseUnitClient()
        val transformResult = specificClient.transform(parseResult.baseUnit, pipelineId)
        assertion(transformResult.baseUnit)
      }
    }
  }

  def getFirstOperation(bu: BaseUnit) =
    bu.asInstanceOf[Document]
      .encodes
      .asInstanceOf[WebApi]
      .endPoints
      .head
      .operations
      .head

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
    modelAssertion(s"$basePath/raml/api-with-annotations.raml") { bu =>
      val ramlAnnotations = getFirstOperation(bu).customDomainProperties
      ramlAnnotations.length shouldBe 2
      val oasTransformResult = oasClient.transform(bu, PipelineId.Compatibility)
      val oasAnnotations     = getFirstOperation(oasTransformResult.baseUnit).customDomainProperties
      oasAnnotations.length shouldBe 2
    }
  }

  // github issue #1121
  test("Declared Raml type with Json Schema should inherit type from it") {
    val ramlApi = s"$basePath/raml/json-schema-scalar-type/json-schema-with-scalar-type.raml"
    modelAssertion(ramlApi, parseOnly = true) { bu =>
      val jsonSchemaType = "http://www.w3.org/2001/XMLSchema#string"
      val declaredTypeWithJsonSchemaNode =
        bu.asInstanceOf[Document].declares.head.asInstanceOf[ScalarShape]
      declaredTypeWithJsonSchemaNode.dataType.value() shouldBe jsonSchemaType
    }
  }

  test("Declared Raml type with Json Schema in external file should inherit type from it") {
    val ramlApi = s"$basePath/raml/json-schema-scalar-type/json-schema-with-scalar-type-in-external-file.raml"
    modelAssertion(ramlApi, parseOnly = true) { bu =>
      val jsonSchemaType = "http://www.w3.org/2001/XMLSchema#string"
      val declaredTypeWithJsonSchemaNode =
        bu.asInstanceOf[Document].declares.head.asInstanceOf[ScalarShape]
      declaredTypeWithJsonSchemaNode.dataType.value() shouldBe jsonSchemaType
    }
  }

  // github issue #1163
  test("Simple inheritance should not delete documentation fields") {
    modelAssertion(s"$basePath/raml/api-with-types.raml", PipelineId.Editing) { bu =>
      val haveNoExamples =
        bu.asInstanceOf[Document]
          .declares
          .filter(s => s.asInstanceOf[AnyShape].examples.isEmpty)
          .map(_.asInstanceOf[AnyShape].name.value())
      val shouldNotHaveExamples = Seq("complex-inheritance-obj", "complex-inheritance-string")
      haveNoExamples == shouldNotHaveExamples shouldBe true
    }
  }

  test("Simple inheritance should not delete documentation fields in uri params") {
    modelAssertion(s"$basePath/raml/uri-params/uri-params.raml") { bu =>
      val parameters    = bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi].endPoints.head.parameters.toList
      val paramExamples = parameters.map(p => p.schema.asInstanceOf[ScalarShape].examples)
      paramExamples.count(_.isEmpty) shouldBe 0
    }
  }
}
