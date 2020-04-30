package amf.cycle

import amf.core.remote.{Amf, RamlYamlHint}
import amf.io.FunSuiteCycleTests

class RamlArrayCycleTest extends FunSuiteCycleTests {
  override def basePath: String =
    "/Users/tfernandez/mulesoft/amf/amf-client/shared/src/test/resources/parser/array-type-expressions/"

  test("Type expression and explicit array must be parsed similarly") {
    cycle("base-type-array.raml", "base-type-array.jsonld", RamlYamlHint, Amf)
  }

  test("Array type expression with inheritance") {
    cycle("type-expression-with-inheritance.raml", "type-expression-with-inheritance.jsonld", RamlYamlHint, Amf)
  }

  test("Union type expression is parsed similar to explicit union array") {
    cycle("union-type-array.raml", "union-type-array.jsonld", RamlYamlHint, Amf)
  }

  test("Matrix type expression and explicit matrix must be parsed similarly") {
    cycle("matrix-type-array.raml", "matrix-type-array.jsonld", RamlYamlHint, Amf)
  }
}
