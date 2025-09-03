package amf

import amf.brokergroup.internal.plugins.parse.entry.BrokerGroupSchemaVersionEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class BrokerGroupSchemaEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-broker-group/shared/src/test/resources/instances/entry/"

  test("BrokerGroup with valid ansVersion") {
    val maybeVersion = BrokerGroupSchemaVersionEntry.apply(getRoot(basePath + "valid.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "0.1.0-beta"
  }

  // Given that the versions are still not fixed, we are not validating it in the entry parser. It will be validated with the schema validation
  test("BrokerGroup with other ansVersion") {
    val maybeVersion = BrokerGroupSchemaVersionEntry.apply(getRoot(basePath + "other.json"))
    maybeVersion.nonEmpty shouldBe true
    maybeVersion.get.version shouldBe "1.0.0"
  }

  test("BrokerGroup without schema version") {
    val maybeVersion = BrokerGroupSchemaVersionEntry.apply(getRoot(basePath + "none.json"))
    maybeVersion.nonEmpty shouldBe false
  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
