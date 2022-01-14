package amf.shapes.internal.spec.jsonschema.ref

import amf.shapes.internal.spec.common.JSONSchemaDraft4SchemaVersion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class LazyAstIndexTest extends AnyFunSuite with Matchers with IndexHelper {

  test("Verify lazy resolution of ids with fragments") {
    val pathToFile = "amf-api-contract/shared/src/test/resources/ast-index/draft-4-spec-example.json"
    val index      = obtainIndex(pathToFile, JSONSchemaDraft4SchemaVersion)

    val ref = "http://x.y.z/otherschema.json#/nested"

    assert(index.valueInMap(ref).isEmpty)

    assert(index.getNode(ref).nonEmpty)

    assert(index.valueInMap(ref).nonEmpty)
  }
}
