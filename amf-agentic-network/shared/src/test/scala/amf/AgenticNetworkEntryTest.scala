package amf

import amf.agenticnetwork.internal.plugins.parse.entry.AgenticNetworkIdEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.YamlParser

class AgenticNetworkEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-agentic-network/shared/src/test/resources/instances/entry/"

  test("AgenticNetwork with string agenticNetwork") {
    val maybeVersion = AgenticNetworkIdEntry.apply(getRoot(basePath + "any-string.yaml"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "2.0.0"
  }

  test("AgenticNetwork with number agenticNetwork") {
    val maybeVersion = AgenticNetworkIdEntry.apply(getRoot(basePath + "any-number.yaml"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "2.0.0"
  }

  test("AgenticNetwork without protocol entry") {
    val maybeVersion = AgenticNetworkIdEntry.apply(getRoot(basePath + "none.yaml"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/yaml`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    YamlParser(content).documents().head
  }
}
