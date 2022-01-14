package amf.validation

import amf.apicontract.client.scala._
import amf.apicontract.client.scala.model.domain.api.{AsyncApi, WebApi}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Payload, Response}
import amf.core.client.common.position.Position
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.ExternalSourceElement
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, SchemaShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

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

  def modelAssertion(path: String, pipelineId: String = PipelineId.Default, transform: Boolean = true)(
      assertion: BaseUnit => Assertion): Future[Assertion] = {
    val parser = APIConfiguration.API().baseUnitClient()
    parser.parse(path) flatMap { parseResult =>
      if (!transform) assertion(parseResult.baseUnit)
      else {
        val specificClient  = configFor(parseResult.sourceSpec).baseUnitClient()
        val transformResult = specificClient.transform(parseResult.baseUnit, pipelineId)
        assertion(transformResult.baseUnit)
      }
    }
  }

  class BaseUnitComponents(isWebApi: Boolean = true) {
    def getApi(bu: BaseUnit) =
      if (isWebApi) bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      else bu.asInstanceOf[Document].encodes.asInstanceOf[AsyncApi]

    def getFirstEndpoint(bu: BaseUnit): EndPoint = getApi(bu).endPoints.head

    def getFirstOperation(bu: BaseUnit): Operation = getFirstEndpoint(bu).operations.head

    def getFirstResponse(bu: BaseUnit): Response = getFirstOperation(bu).responses.head

    def getFirstPayload(bu: BaseUnit): Payload = getFirstResponse(bu).payloads.head

    def getFirstPayloadSchema(bu: BaseUnit): ScalarShape = getFirstPayload(bu).schema.asInstanceOf[ScalarShape]
  }

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
      val components      = new BaseUnitComponents
      val ramlAnnotations = components.getFirstOperation(bu).customDomainProperties
      ramlAnnotations.length shouldBe 2
      val oasTransformResult = oasClient.transform(bu, PipelineId.Compatibility)
      val oasAnnotations     = components.getFirstOperation(oasTransformResult.baseUnit).customDomainProperties
      oasAnnotations.length shouldBe 2
    }
  }

  // github issue #1121
  test("Declared Raml type with Json Schema should inherit type from it") {
    val ramlApi = s"$basePath/raml/json-schema-scalar-type/json-schema-with-scalar-type.raml"
    modelAssertion(ramlApi, transform = false) { bu =>
      val jsonSchemaType = "http://www.w3.org/2001/XMLSchema#string"
      val declaredTypeWithJsonSchemaNode =
        bu.asInstanceOf[Document].declares.head.asInstanceOf[ScalarShape]
      declaredTypeWithJsonSchemaNode.dataType.value() shouldBe jsonSchemaType
    }
  }

  test("Declared Raml type with Json Schema in external file should inherit type from it") {
    val ramlApi = s"$basePath/raml/json-schema-scalar-type/json-schema-with-scalar-type-in-external-file.raml"
    modelAssertion(ramlApi, transform = false) { bu =>
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

  test("RAML 08 should not delete empty example") {
    val api08 = s"$basePath/raml/empty-example-08.raml"
    val api10 = s"$basePath/raml/empty-example.raml"
    raml08Client.parse(api08) flatMap { parseResult08 =>
      ramlClient.parse(api10) flatMap { parseResult10 =>
        val components = new BaseUnitComponents
        val examplesField08 =
          components.getFirstPayloadSchema(parseResult08.baseUnit).fields.get(AnyShapeModel.Examples)
        val examplesField =
          components.getFirstPayloadSchema(parseResult10.baseUnit).fields.get(AnyShapeModel.Examples)
        val range08 = examplesField08.annotations.lexical()
        val range   = examplesField.annotations.lexical()
        range08 shouldEqual range
      }
    }
  }

  test("different datatypes should not fall under simple inheritance") {
    val api = s"$basePath/raml/merge-resourceType.raml"
    ramlClient.parse(api) flatMap { parseResult =>
      val transformResult = ramlClient.transform(parseResult.baseUnit, PipelineId.Editing)
      // parsing or resolution results are not relevant
      ramlClient.validate(transformResult.baseUnit) map (report => report.results.size shouldBe 0)
    }
  }

  test("inline shapes should not include range of parent key") {
    val api = s"$basePath/annotations/inline-shape.yaml"
    modelAssertion(api, transform = false) { bu =>
      val components             = new BaseUnitComponents(false)
      val payload                = components.getFirstPayload(bu)
      val schema                 = payload.schema.asInstanceOf[NodeShape]
      val ifField                = schema.fields.fields().find(_.field.toString().endsWith("if")).get
      val inlineShape            = ifField.value.value
      val inlineShapeAnnotations = inlineShape.annotations
      inlineShapeAnnotations.lexical().start shouldBe Position(12, 12)
      inlineShapeAnnotations.lexical().end shouldBe Position(14, 26)
    }
  }
}
