package amf.cycle

import amf.core.remote.Oas20JsonHint
import amf.core.remote.Vendor.OAS20
import amf.io.FunSuiteCycleTests

class Oas20CycleTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/oas20/"

  test("Invalid oas type with non-integer minimum doesn't throw exception in emission") {
    cycle("json/invalid-type-with-string-minimum.json",
          "json/invalid-type-with-string-minimum.cycled.json",
          Oas20JsonHint,
          OAS20)
  }
}
