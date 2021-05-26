package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.client.environment.WebAPIConfiguration
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.parser.{ParserContext, YMapOps}
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.AliasCounter
import amf.plugins.document.webapi.contexts.parser.async.Async20WebApiContext
import amf.plugins.document.webapi.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.webapi.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion
}
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, FunSuite, Matchers}
import org.yaml.model.{YMap, YNode}
import org.yaml.parser.JsonParser

class AstIndexTest extends FunSuite with Matchers with IndexHelper {

  test("Json Schema Draft 4 - Id resolution test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/draft-4-spec-example.json"
    val expected = Seq(
      IndexResult("#"),
      IndexResult("http://x.y.z/rootschema.json#"),
      IndexResult("#/schema1"),
      IndexResult("http://x.y.z/rootschema.json#/schema1", MapAssertion(Seq("id", "schema1Key"))),
      IndexResult("http://x.y.z/rootschema.json#foo"),
      IndexResult("#/schema2"),
      IndexResult("http://x.y.z/rootschema.json#/schema2"),
      IndexResult("http://x.y.z/otherschema.json#"),
      IndexResult("#/schema2/nested"),
      IndexResult("http://x.y.z/otherschema.json#/nested", MapAssertion(Seq("id", "nestedKey"))),
      IndexResult("http://x.y.z/otherschema.json#/nested/seqValue/0/key", MapAssertion(Seq("inSeq"))),
      IndexResult("http://x.y.z/otherschema.json#bar"),
      IndexResult("#/schema2/alsonested"),
      IndexResult("http://x.y.z/t/inner.json#a"),
      IndexResult("#/schema3"),
      IndexResult("some://where.else/completely#")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Json Schema Draft 7 - $id resolution test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/draft-7-spec-example.json"
    val expected = Seq(
      IndexResult("#"),
      IndexResult("http://example.com/root.json#"),
      IndexResult("#/definitions/A"),
      IndexResult("http://example.com/root.json#foo"),
      IndexResult("#/definitions/B"),
      IndexResult("http://example.com/other.json#"),
      IndexResult("http://example.com/other.json#/definitions", MapAssertion(Seq("X", "Y"))),
      IndexResult("#/definitions/B/definitions"),
      IndexResult("#/definitions/B/definitions/X"),
      IndexResult("http://example.com/other.json#bar"),
      IndexResult("#/definitions/B/definitions/Y"),
      IndexResult("http://example.com/t/inner.json"),
      IndexResult("#/definitions/C"),
      IndexResult("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f#")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft7SchemaVersion)
  }

  test("Json Schema Draft 2019-09 - $id and $anchor resolution test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/draft-2019-spec-example.json"
    val expected = Seq(
      IndexResult("#"),
      IndexResult("https://example.com/root.json"),
      IndexResult("https://example.com/root.json#"),
      IndexResult("https://example.com/root.json#foo"),
      IndexResult("https://example.com/root.json#/$defs/A"),
      IndexResult("https://example.com/root.json#/$defs/B"),
      IndexResult("https://example.com/other.json#bar"),
      IndexResult("https://example.com/other.json#/$defs/X", MapAssertion(Seq("$anchor"))),
      IndexResult("https://example.com/t/inner.json#"),
      IndexResult("https://example.com/t/inner.json#bar"),
      IndexResult("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f"),
      IndexResult("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f#")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft201909SchemaVersion)
  }

  test("Scalar map") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/scalar-map.json"
    val expected = Seq(
      IndexResult("/"),
      IndexResult("/a"),
      IndexResult("/b")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Small map") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/small-map.json"
    val expected = Seq(
      IndexResult("/"),
      IndexResult("/a"),
      IndexResult("/a/ax"),
      IndexResult("/a/az"),
      IndexResult("/b"),
      IndexResult("/b/bx"),
      IndexResult("/b/bz")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Entry with seq") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/entry-with-seq.json"
    val expected = Seq(
      IndexResult("/"),
      IndexResult("/a"),
      IndexResult("/a/0"),
      IndexResult("/a/1"),
      IndexResult("/a/2")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Inner array reference test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/self-reference-in-array.json"
    val expected = Seq(
      IndexResult("/"),
      IndexResult("/items"),
      IndexResult("/items/0"),
      IndexResult("/items/1")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Inner seq elements by index") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/inner-seq-elements-by-index.json"
    val expected = Seq(
      IndexResult("/"),
      IndexResult("/seq"),
      IndexResult("/seq/0/a"),
      IndexResult("/seq/0/b"),
      IndexResult("/seq/0/b/c"),
      IndexResult("/seq/1/a"),
      IndexResult("/seq/1/b"),
      IndexResult("/seq/1/b/c")
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  private def runIndex(pathToFile: String, expected: Seq[IndexResult], version: JSONSchemaVersion): Assertion = {
    val index = obtainIndex(pathToFile, version)
    val foundReferences = expected.filter(e => {
      val result = index.getNode(e.path)
      assertValue(result, e)
      result.isDefined
    })
    foundReferences.size shouldBe expected.size
  }

  private def assertValue(result: Option[YMapEntryLike], expected: IndexResult): Unit =
    result.foreach { foundNode =>
      expected.valueAssertion.foreach(_(foundNode.value))
    }

}

trait IndexHelper extends PlatformSecrets {
  def obtainIndex(pathToFile: String, version: JSONSchemaVersion): AstIndex = {
    val content = platform.fs.syncFile(pathToFile).read()
    val doc     = JsonParser(content).document()
    val ctx =
      new Async20WebApiContext(
        "loc",
        Seq(),
        ParserContext(
          config =
            WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler).parseConfiguration))
    AstIndexBuilder.buildAst(doc.node, AliasCounter(), version)(WebApiShapeParserContextAdapter(ctx))
  }
}

case class IndexResult(path: String, valueAssertion: Option[YNode => Assertion] = None)

object IndexResult {
  def apply(path: String, valueAssertion: YNode => Assertion): IndexResult = IndexResult(path, Some(valueAssertion))
}

case class MapAssertion(keys: Seq[String]) extends Function1[YNode, Assertion] {
  override def apply(result: YNode): Assertion = {
    val map = result.as[YMap]
    keys.forall(map.key(_).isDefined) shouldEqual true
  }
}
