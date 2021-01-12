package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.plugins.document.webapi.parser.spec.declaration.JSONSchemaDraft4SchemaVersion
import org.scalatest.{FunSuite, Matchers}

class LazyAstIndexTest extends FunSuite with Matchers with IndexHelper {

  test("Verify lazy resolution of ids with fragments") {
    val pathToFile = "amf-webapi/shared/src/test/resources/ast-index/draft-4-spec-example.json"
    val index      = obtainIndex(pathToFile, JSONSchemaDraft4SchemaVersion)

    val ref = "http://x.y.z/otherschema.json#/nested"

    assert(index.valueInMap(ref).isEmpty)

    assert(index.getNode(ref).nonEmpty)

    assert(index.valueInMap(ref).nonEmpty)
  }
}
