package amf

import amf.agentgraph.internal.plugins.parse.entry.AgentGraphIdEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.YamlParser

class AgentGraphEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-agent-graph/shared/src/test/resources/instances/entry/"

  test("AgentGraph with string agentGraph") {
    val maybeVersion = AgentGraphIdEntry.apply(getRoot(basePath + "any-string.yaml"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "2.0.0"
  }

  test("AgentGraph with number agentGraph") {
    val maybeVersion = AgentGraphIdEntry.apply(getRoot(basePath + "any-number.yaml"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "2.0.0"
  }

  test("AgentGraph without protocol entry") {
    val maybeVersion = AgentGraphIdEntry.apply(getRoot(basePath + "none.yaml"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/yaml`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    YamlParser(content).documents().head
  }
}
