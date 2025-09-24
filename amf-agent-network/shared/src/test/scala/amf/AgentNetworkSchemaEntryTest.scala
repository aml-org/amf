package amf

import amf.agentnetwork.internal.plugins.parse.entry.AgentNetworkSchemaVersionEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class AgentNetworkSchemaEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-agent-network/shared/src/test/resources/instances/entry/"

  test("AgentNetwork with valid ansVersion") {
    val maybeVersion = AgentNetworkSchemaVersionEntry.apply(getRoot(basePath + "valid.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "1.0.0"
  }

  // Given that the versions are still not fixed, we are not validating it in the entry parser. It will be validated with the schema validation
  test("AgentNetwork with other ansVersion") {
    val maybeVersion = AgentNetworkSchemaVersionEntry.apply(getRoot(basePath + "other.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "1.1.0"
  }

  test("AgentNetwork without schema version") {
    val maybeVersion = AgentNetworkSchemaVersionEntry.apply(getRoot(basePath + "none.json"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
