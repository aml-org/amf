package amf.cycle

import amf.core.remote.Vendor.OAS20
import amf.core.remote.{OasJsonHint, Vendor}
import amf.io.FunSuiteCycleTests

class Oas20CycleTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-client/shared/src/test/resources/upanddown/cycle/oas20/"

  test("Invalid oas type with non-integer minimum doesn't throw exception in emission") {
    cycle("json/invalid-type-with-string-minimum.json", "json/invalid-type-with-string-minimum.cycled.json", OasJsonHint, OAS20)
  }
}
