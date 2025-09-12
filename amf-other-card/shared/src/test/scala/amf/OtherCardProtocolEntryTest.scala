package amf

import amf.othercard.internal.plugins.parse.entry.OtherCardIdEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class OtherCardProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-other-card/shared/src/test/resources/instances/entry/"

//  test("OtherCard with string protocolVersion") {
//    val maybeVersion = OtherCardIdEntry.apply(getRoot(basePath + "any-string.json"))
//    maybeVersion.nonEmpty shouldBe true
//    maybeVersion.get.version shouldBe "0.1"
//  }
//
//  test("OtherCard with number protocolVersion") {
//    val maybeVersion = OtherCardIdEntry.apply(getRoot(basePath + "any-number.json"))
//    maybeVersion.nonEmpty shouldBe true
//    maybeVersion.get.version shouldBe "0.1"
//  }
//
//  test("OtherCard without protocol entry") {
//    val maybeVersion = OtherCardIdEntry.apply(getRoot(basePath + "none.json"))
//    maybeVersion.nonEmpty shouldBe false
//  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
