package amf

import amf.llmmetadata.internal.plugins.parse.entry.LLMMetadataIdEntry
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import org.mulesoft.common.io.Fs
import org.scalatest.matchers.should.Matchers
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser

class LLMMetadataProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String = "amf-llm-metadata/shared/src/test/resources/instances/entry/"

//  test("LLMMetadata with string protocolVersion") {
//    val maybeVersion = LLMMetadataIdEntry.apply(getRoot(basePath + "any-string.json"))
//    maybeVersion.nonEmpty shouldBe true
//    maybeVersion.get.version shouldBe "0.1"
//  }
//
//  test("LLMMetadata with number protocolVersion") {
//    val maybeVersion = LLMMetadataIdEntry.apply(getRoot(basePath + "any-number.json"))
//    maybeVersion.nonEmpty shouldBe true
//    maybeVersion.get.version shouldBe "0.1"
//  }
//
//  test("LLMMetadata without protocol entry") {
//    val maybeVersion = LLMMetadataIdEntry.apply(getRoot(basePath + "none.json"))
//    maybeVersion.nonEmpty shouldBe false
//  }

  private def getRoot(path: String): Root =
    Root(SyamlParsedDocument(getYDocument(path)), "", Mimes.`application/json`, Nil, UnspecifiedReference, "")

  private def getYDocument(path: String): YDocument = {
    val content = Fs.syncFile(path).read()
    JsonParser(content).documents().head
  }
}
