package amf.shapes.client.jsonldschema.parsing

import amf.core.client.scala.config.RenderOptions
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDObject}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class JsonLDSchemaParsingTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/parsing"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def run(testName: String, assertions: (JsonLDInstanceDocument) => Assertion): Future[Assertion] = {
    for {
      jsonDocument <- client.parseJsonLDSchema(s"file://$basePath/$testName/schema.json").map(_.jsonDocument)
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
      val propByMetadata = instance.encodes.head
        .asInstanceOf[JsonLDObject]
        .fields
        .getValueAsOption("anypoint://vocabulary/policy.yaml#sensitive")
      propByMetadata.nonEmpty shouldBe true
      propByMetadata.get.value.toString shouldBe "something"
    }

    run("characteristics-in-property", assertions)
  }

  test("Object with characteristics in one array property") {
    def assertions(instance: JsonLDInstanceDocument): Assertion = {
      instance.encodes.headOption.nonEmpty shouldBe true
      instance.encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val propByMetadata = instance.encodes.head
        .asInstanceOf[JsonLDObject]
        .fields
        .getValueAsOption("anypoint://vocabulary/policy.yaml#sensitive")
      propByMetadata.nonEmpty shouldBe true
      propByMetadata.get.value.isInstanceOf[JsonLDArray] shouldBe true
      val dArray = propByMetadata.get.value.asInstanceOf[JsonLDArray]
      dArray.values.nonEmpty shouldBe true
      dArray.values.head.toString shouldBe "something"
    }

    run("characteristics-in-property-array", assertions)
  }

  test("Object with characteristics in pattern property with no match") {
    def assertions(instance: JsonLDInstanceDocument): Assertion = {
      instance.encodes.headOption.nonEmpty shouldBe true
      instance.encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val encoded = instance.encodes.head.asInstanceOf[JsonLDObject]
      val propByMetadataLiteral = encoded.fields.getValueAsOption("anypoint://vocabulary/policy.yaml#sensitive")
      val propByMetadataPattern = encoded.fields.getValueAsOption("anypoint://vocabulary/policy.yaml#pattern")
      propByMetadataLiteral.nonEmpty shouldBe true
      propByMetadataPattern.nonEmpty shouldBe false
      propByMetadataLiteral.get.value.toString shouldBe "something"
    }

    run("pattern-property-simple-with-semantic", assertions)
  }

  test("Object with characteristics in pattern property with match") {
    def assertions(instance: JsonLDInstanceDocument): Assertion = {
      instance.encodes.nonEmpty shouldBe true
      instance.encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val encoded = instance.encodes.head.asInstanceOf[JsonLDObject]
      val propByMetadataLiteral = encoded.fields.getValueAsOption("anypoint://vocabulary/policy.yaml#sensitive")
      val propByMetadataPattern = encoded.fields.getValueAsOption("anypoint://vocabulary/policy.yaml#pattern")
      propByMetadataPattern.nonEmpty shouldBe true
      propByMetadataLiteral.nonEmpty shouldBe false
      propByMetadataPattern.get.value.toString shouldBe "something"
    }

    run("pattern-property-with-semantic-match", assertions)
  }

}
