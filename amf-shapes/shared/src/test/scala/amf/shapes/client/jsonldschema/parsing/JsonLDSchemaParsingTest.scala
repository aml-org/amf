package amf.shapes.client.jsonldschema.parsing

import amf.core.client.scala.config.RenderOptions
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDObject}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class JsonLDSchemaParsingTest extends AsyncFunSuite with Matchers {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/parsing"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def run(testName: String, assertions: (JsonLDInstanceDocument) => Assertion): Future[Assertion] = {
    for {
      jsonDocument   <- client.parseJsonLDSchema(s"file://$basePath/$testName/schema.json").map(_.jsonDocument)
      instanceResult <- client.parseJsonLDInstance(s"file://$basePath/$testName/instance.json", jsonDocument)
    } yield {
      instanceResult.conforms shouldBe true
      instanceResult.baseUnit == null shouldBe false
      instanceResult.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      assertions(instanceResult.baseUnit.asInstanceOf[JsonLDInstanceDocument])
    }
  }

  test("Object with characteristics in one property") {
    def assertions(instance: JsonLDInstanceDocument): Assertion = {
      instance.encodes.headOption.nonEmpty shouldBe true
      instance.encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val propByMetadata = instance.encodes.head.asInstanceOf[JsonLDObject].fields.getValueAsOption("config:sensitive")
      propByMetadata.nonEmpty shouldBe true
      propByMetadata.get.value.toString shouldBe "something"
    }
    run("characteristics-in-property", assertions)
  }

  test("Object with characteristics in one array property") {
    def assertions(instance: JsonLDInstanceDocument): Assertion = {
      instance.encodes.headOption.nonEmpty shouldBe true
      instance.encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val propByMetadata = instance.encodes.head.asInstanceOf[JsonLDObject].fields.getValueAsOption("config:sensitive")
      propByMetadata.nonEmpty shouldBe true
      propByMetadata.get.value.isInstanceOf[JsonLDArray] shouldBe true
      val dArray = propByMetadata.get.value.asInstanceOf[JsonLDArray]
      dArray.values.nonEmpty shouldBe true
      dArray.values.head.toString shouldBe "something"
    }

    run("characteristics-in-property-array", assertions)
  }

}
