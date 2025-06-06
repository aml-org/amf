package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class MCPSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that MCP Schema has no errors") {
    MCPSchemaLoader.doc != null shouldBe true
    MCPSchemaLoader.schema != null shouldBe true
    MCPSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    MCPSchemaLoader.errors.size shouldBe 0
  }
}
