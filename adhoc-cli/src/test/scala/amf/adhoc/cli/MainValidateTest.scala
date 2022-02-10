package amf.adhoc.cli

import org.scalatest.matchers.should.Matchers

class MainValidateTest extends org.scalatest.funsuite.AnyFunSuite with Matchers {

  test("valid validation profile") {
    val result = Main.validateInstance("adhoc-cli/src/test/resources/profile/valid.yaml")
    assert(result.conforms)
  }

  test("invalid validation profile") {
    val result = Main.validateInstance("adhoc-cli/src/test/resources/profile/invalid.yaml")
    assert(!result.conforms)
  }

  test("valid report instance") {
    val result = Main.validateInstance("adhoc-cli/src/test/resources/report/valid.jsonld")
    assert(result.conforms)
  }

  test("invalid report instance") {
    val result = Main.validateInstance("adhoc-cli/src/test/resources/report/invalid.jsonld")
    assert(!result.conforms)
  }

}
