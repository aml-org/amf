package amf.shapes.client.jsonldschema.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.InvalidCharacteristicsUse
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

class SemanticContextTransformationTest extends AsyncFunSuite with FileAssertionTest with Matchers {

  private lazy val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/semantic-context/"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  test(s"Test invalid characteristics at root") {
    transform("characteristics-at-root.json").map { transformationResult =>
      transformationResult.conforms shouldBe (false)
      transformationResult.results.length shouldBe (1)
      transformationResult.results.head.message shouldBe (InvalidCharacteristicsUse.message)
      transformationResult.results.head.validationId shouldBe (InvalidCharacteristicsUse.id)
    }
  }

  test(s"Test invalid characteristics at array items from property range") {
    transform("characteristics-at-array-item.json").map { transformationResult =>
      transformationResult.conforms shouldBe (false)
      transformationResult.results.length shouldBe (1)
      transformationResult.results.head.message shouldBe (InvalidCharacteristicsUse.message)
      transformationResult.results.head.validationId shouldBe (InvalidCharacteristicsUse.id)
    }
  }

  private def transform(source: String) = {
    client
      .parseJsonLDSchema("file://" + basePath + source)
      .map(r => client.transform(r.jsonDocument, PipelineId.Editing))
  }
}
