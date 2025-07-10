package amf

import amf.agentfabric.internal.plugins.parse.entry.AgentFabricApiVersionEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class AgentFabricProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-agent-fabric/shared/src/test/resources/instances/entry/"

  test("AgentFabric with string apiVersion") {
    val maybeVersion = AgentFabricApiVersionEntry.apply(getRoot(basePath + "any-string.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "0.1"
  }

  test("AgentFabric with number apiVersion") {
    val maybeVersion = AgentFabricApiVersionEntry.apply(getRoot(basePath + "any-number.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "0.1"
  }

  test("AgentFabric without protocol entry") {
    val maybeVersion = AgentFabricApiVersionEntry.apply(getRoot(basePath + "none.json"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
