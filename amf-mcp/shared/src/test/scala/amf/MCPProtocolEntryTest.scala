package amf

import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import amf.mcp.internal.plugins.parse.{MCP20241105ProtocolVersion, MCP20250326ProtocolVersion, MCPProtocolEntry}
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class MCPProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-mcp/shared/src/test/resources/instances/entry/"

  test("MCP with valid 2024-11-05 entry") {
    val maybeVersion = MCPProtocolEntry.apply(getRoot(basePath + "2024.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get shouldBe MCP20241105ProtocolVersion
  }

  test("MCP with valid 2025-03-26 entry") {
    val maybeVersion = MCPProtocolEntry.apply(getRoot(basePath + "2025.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get shouldBe MCP20250326ProtocolVersion
  }

  test("MCP without protocol entry") {
    val maybeVersion = MCPProtocolEntry.apply(getRoot(basePath + "none.json"))
    maybeVersion.nonEmpty shouldBe false
  }

  test("MCP with incorrect protocol entry") {
    val maybeVersion = MCPProtocolEntry.apply(getRoot(basePath + "wrong.json"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
