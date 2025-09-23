package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.llmmetadata.internal.plugins.parse.schema.LLMMetadataSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class LLMMetadataSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that LLMMetadata Schema has no errors") {
    LLMMetadataSchemaLoader.doc != null shouldBe true
    LLMMetadataSchemaLoader.schema != null shouldBe true
//    LLMMetadataSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true // TODO returns AnyShape
    LLMMetadataSchemaLoader.errors.size shouldBe 0
  }
}
