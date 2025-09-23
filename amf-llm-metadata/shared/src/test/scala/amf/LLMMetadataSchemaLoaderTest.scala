package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.llmmetadata.internal.plugins.parse.schema.LLMMetadataSchemaLoader
import amf.shapes.client.scala.model.domain.AnyShape
import org.scalatest.matchers.should.Matchers

class LLMMetadataSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that LLMMetadata Schema has no errors") {
    LLMMetadataSchemaLoader.doc != null shouldBe true
    LLMMetadataSchemaLoader.schema != null shouldBe true
    LLMMetadataSchemaLoader.schema.isInstanceOf[AnyShape] shouldBe true
    LLMMetadataSchemaLoader.errors.size shouldBe 0
  }
}
