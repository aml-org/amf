package amf.shapes.client.jsonldschema.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFResult
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationResult
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  InvalidCharacteristicsUse,
  UnsupportedContainer
}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class SemanticContextTransformationTest extends AsyncFunSuite with FileAssertionTest with Matchers {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private lazy val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/transformation/"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def transform(source: String): Future[AMFResult] = {
    client
      .parseJsonLDSchema("file://" + basePath + source)
      .map(r => client.transform(r.jsonDocument, PipelineId.Editing))
  }
  private def shouldHaveCharacteristicsUseError(results: Seq[AMFValidationResult]): Assertion = {
    results.length shouldBe (1)
    results.head.message shouldBe (InvalidCharacteristicsUse.message)
    results.head.validationId shouldBe (InvalidCharacteristicsUse.id)
  }

  private def shouldHaveSemanticMapping(bu: BaseUnit): Assertion = {
    val propertyRange = getAnnotatedProperty(bu)
    propertyRange.semanticContext.isDefined shouldBe true
    propertyRange.semanticContext.get.overrideMappings.head.value() shouldBe "alias:A"
  }
  private def getAnnotatedProperty(bu: BaseUnit): AnyShape = bu
    .asInstanceOf[JsonSchemaDocument]
    .encodes
    .asInstanceOf[NodeShape]
    .properties
    .head
    .range
    .asInstanceOf[AnyShape]

  test(s"Test invalid characteristics at root") {
    transform("characteristics/invalid-at-root.json").map { transformationResult =>
      transformationResult.conforms shouldBe false
      shouldHaveCharacteristicsUseError(transformationResult.results)
    }
  }

  test(s"Test invalid characteristics at array items from property range") {
    transform("characteristics/invalid-at-array-item.json").map { transformationResult =>
      transformationResult.conforms shouldBe false
      shouldHaveCharacteristicsUseError(transformationResult.results)
    }
  }

  test(s"Test invalid characteristics at declared array") {
    transform("characteristics/invalid-at-declared-array.json").map { transformationResult =>
      transformationResult.conforms shouldBe false
      shouldHaveCharacteristicsUseError(transformationResult.results)
    }
  }

  test(s"Test invalid characteristics at declared object") {
    transform("characteristics/invalid-at-declared-object.json").map { transformationResult =>
      transformationResult.conforms shouldBe false
      shouldHaveCharacteristicsUseError(transformationResult.results)
    }
  }

  test(s"Test invalid characteristics at declared scalar") {
    transform("characteristics/invalid-at-declared-scalar.json").map { transformationResult =>
      transformationResult.conforms shouldBe false
      shouldHaveCharacteristicsUseError(transformationResult.results)
    }
  }

  test(s"Test valid characteristics at property array") {
    transform("characteristics/valid-at-property-array.json").map { transformationResult =>
      transformationResult.conforms shouldBe true
      shouldHaveSemanticMapping(transformationResult.baseUnit)
    }
  }

  test(s"Test valid characteristics at property object") {
    transform("characteristics/valid-at-property-object.json").map { transformationResult =>
      transformationResult.conforms shouldBe true
      shouldHaveSemanticMapping(transformationResult.baseUnit)
    }
  }

  test(s"Test valid characteristics at property scalar") {
    transform("characteristics/valid-at-property-scalar.json").map { transformationResult =>
      transformationResult.conforms shouldBe true
      shouldHaveSemanticMapping(transformationResult.baseUnit)
    }
  }

  test(s"Test invalid @container value") {
    transform("invalid-container-value.json").map { transformationResult =>
      transformationResult.conforms shouldBe false
      transformationResult.results.length shouldBe (1)
      transformationResult.results.head.message shouldBe (s"${UnsupportedContainer.message}. Supported values are: @list")
      transformationResult.results.head.validationId shouldBe (UnsupportedContainer.id)
    }
  }

}
