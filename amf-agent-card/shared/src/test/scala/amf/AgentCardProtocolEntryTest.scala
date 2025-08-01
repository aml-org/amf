package amf

import amf.agentcard.internal.plugins.parse.entry.AgentCardProtocolVersionEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class AgentCardProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-agent-card/shared/src/test/resources/instances/entry/"

  test("AgentCard with string protocolVersion") {
    val maybeVersion = AgentCardProtocolVersionEntry.apply(getRoot(basePath + "any-string.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "0.1"
  }

  test("AgentCard with number protocolVersion") {
    val maybeVersion = AgentCardProtocolVersionEntry.apply(getRoot(basePath + "any-number.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "0.1"
  }

  test("AgentCard without protocol entry") {
    val maybeVersion = AgentCardProtocolVersionEntry.apply(getRoot(basePath + "none.json"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
