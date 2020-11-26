package amf.client.model.domain

import java.net.URI

import amf.core.parser.ParserContext
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.AliasCounter
import amf.plugins.document.webapi.contexts.parser.async.Async20WebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft201909SchemaVersion, JSONSchemaDraft4SchemaVersion, JSONSchemaDraft7SchemaVersion, JSONSchemaVersion}
import amf.plugins.document.webapi.parser.spec.jsonschema.{AstIndexBuilder, Draft2019ResolutionScope, Draft4ResolutionScope, Draft7ResolutionScope, LexicalResolutionScope}
import org.scalatest.{Assertion, FunSuite, Matchers}
import org.yaml.parser.JsonParser

class AstIndexTest extends FunSuite with Matchers with PlatformSecrets{


  test("Json Schema Draft 4 - Id resolution test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/draft-4-spec-example.json"
    val expected = Seq(
      "#",
      "http://x.y.z/rootschema.json#",
      "#/schema1",
      "http://x.y.z/rootschema.json#/schema1",
      "http://x.y.z/rootschema.json#foo",
      "#/schema2",
      "http://x.y.z/rootschema.json#/schema2",
      "http://x.y.z/otherschema.json#",
      "#/schema2/nested",
      "http://x.y.z/otherschema.json#/nested",
      "http://x.y.z/otherschema.json#bar",
      "#/schema2/alsonested",
      "http://x.y.z/t/inner.json#a",
      "#/schema3",
      "some://where.else/completely#"
    ).sorted
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Json Schema Draft 7 - $id resolution test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/draft-7-spec-example.json"
    val expected = Seq(
      "#",
      "http://example.com/root.json#",
      "#/definitions/A",
      "http://example.com/root.json#foo",
      "#/definitions/B",
      "http://example.com/other.json#",
      "#/definitions/B/definitions",
      "#/definitions/B/definitions/X",
      "http://example.com/other.json#bar",
      "#/definitions/B/definitions/Y",
      "http://example.com/t/inner.json",
      "#/definitions/C",
      "urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f#"
    )
    runIndex(pathToFile, expected, JSONSchemaDraft7SchemaVersion)
  }

  test("Scalar map") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/scalar-map.json"
    val expected = Seq(
      "/",
      "/a",
      "/b",
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Small map") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/small-map.json"
    val expected = Seq(
      "/",
      "/a",
      "/a/ax",
      "/a/az",
      "/b",
      "/b/bx",
      "/b/bz"
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Entry with seq") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/entry-with-seq.json"
    val expected = Seq(
      "/",
      "/a",
      "/a/0",
      "/a/1",
      "/a/2"
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  test("Inner array reference test") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/self-reference-in-array.json"
    val expected = Seq(
      "/",
      "/items",
      "/items/0",
      "/items/1"
    )
    runIndex(pathToFile, expected, JSONSchemaDraft4SchemaVersion)
  }

  private def runIndex(pathToFile: String, mustBeInIndex: Seq[String], version: JSONSchemaVersion): Assertion = {
    val content = platform.fs.syncFile(pathToFile).read()
    val doc = JsonParser(content).document()
    val ctx = new Async20WebApiContext("loc", Seq(), ParserContext(eh = UnhandledParserErrorHandler))
    val index = AstIndexBuilder(AliasCounter())(ctx).build(doc.node, Seq(LexicalResolutionScope(), getSpecificEntryBuilderFor(version, new URI(pathToFile))))
    val foundReferences = mustBeInIndex.filter(e => index.getNode(e).isDefined).sorted
    foundReferences.size shouldBe mustBeInIndex.size
  }

  private def getSpecificEntryBuilderFor(version: JSONSchemaVersion, baseUri: URI) = version match {
    case JSONSchemaDraft4SchemaVersion => Draft4ResolutionScope(baseUri)
    case JSONSchemaDraft7SchemaVersion => Draft7ResolutionScope(baseUri)
    case JSONSchemaDraft201909SchemaVersion => Draft2019ResolutionScope(baseUri)
    case _ => throw new IllegalArgumentException(s"There isn't a defined entry builder for version: $version")
  }
}
