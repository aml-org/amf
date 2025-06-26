package amf.shapes.client.jsonldschema.parsing

import amf.core.client.scala.config.{ParsingOptions, RenderOptions}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDObject}
import amf.shapes.internal.annotations.SourceSchemaDef
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class JsonLDSchemaParsingTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/parsing"
  private val defaultConfig: JsonLDSchemaConfiguration =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint)

  private def run(
      testName: String,
      assertions: (JsonLDInstanceDocument) => Assertion,
      configOverride: Option[JsonLDSchemaConfiguration] = None
  ): Future[Assertion] = {
    val client = configOverride.getOrElse(defaultConfig).baseUnitClient()
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
      val encoded               = instance.encodes.head.asInstanceOf[JsonLDObject]
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
      val encoded               = instance.encodes.head.asInstanceOf[JsonLDObject]
      val propByMetadataLiteral = encoded.fields.getValueAsOption("anypoint://vocabulary/policy.yaml#sensitive")
      val propByMetadataPattern = encoded.fields.getValueAsOption("anypoint://vocabulary/policy.yaml#pattern")
      propByMetadataPattern.nonEmpty shouldBe true
      propByMetadataLiteral.nonEmpty shouldBe false
      propByMetadataPattern.get.value.toString shouldBe "something"
    }

    run("pattern-property-with-semantic-match", assertions)
  }

  test("JsonLdElements should have SourceSchemaDef annotation when ParsingOptions is configured") {
    def assertions(instance: JsonLDInstanceDocument): Assertion = {
      instance.encodes.nonEmpty shouldBe true
      instance.encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val encoded               = instance.encodes.head.asInstanceOf[JsonLDObject]
      val encodedAnnotation = encoded.annotations.find(classOf[SourceSchemaDef])
      encodedAnnotation.nonEmpty shouldBe true
      encodedAnnotation.get.definition.isInstanceOf[NodeShape] shouldBe true
      encoded.fields.fields().size shouldBe 2
      val prop1 = encoded.fields.fields().head
      prop1.field.value.iri() shouldBe "http://a.ml/vocabularies/core#prop1"
      val prop1Annotation = prop1.value.annotations.find(classOf[SourceSchemaDef])
      prop1Annotation.nonEmpty shouldBe true
      prop1Annotation.get.definition.isInstanceOf[PropertyShape] shouldBe true
      val prop1ValueAnnotation = prop1.value.value.annotations.find(classOf[SourceSchemaDef])
      prop1ValueAnnotation.nonEmpty shouldBe true
      prop1ValueAnnotation.get.definition.isInstanceOf[ScalarShape] shouldBe true
      val prop2 = encoded.fields.fields().tail.head
      prop2.field.value.iri() shouldBe "http://a.ml/vocabularies/core#prop2"
      val prop2Annotation = prop1.value.annotations.find(classOf[SourceSchemaDef])
      prop2Annotation.nonEmpty shouldBe true
      prop2Annotation.get.definition.isInstanceOf[PropertyShape] shouldBe true
      val prop2ValueAnnotation = prop2.value.value.annotations.find(classOf[SourceSchemaDef])
      prop2ValueAnnotation.nonEmpty shouldBe true
      prop2ValueAnnotation.get.definition.isInstanceOf[ArrayShape] shouldBe true
    }
    val config = defaultConfig.withParsingOptions(ParsingOptions().withSourceSchemaDef)
    run("simple", assertions, Some(config))
  }

}
