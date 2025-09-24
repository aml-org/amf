package amf

import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import amf.mcp.internal.plugins.parse.entry.MCPProtocolEntry
import amf.shapes.internal.plugins.parser.entry.DefaultIdVersion
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

// There is no version entry for MCP
class MCPProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-mcp/shared/src/test/resources/instances/entry/"

  test("MCP without protocol entry") {
    val maybeVersion = MCPProtocolEntry.apply(getRoot(basePath + "none.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get shouldBe DefaultIdVersion
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
