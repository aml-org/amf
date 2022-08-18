package amf.document.apicontract.validation.shaclfunctions

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.validation.shacl.oas.DuplicatedCommonEndpointPathValidation
import amf.validation.internal.shacl.custom.CustomShaclValidator
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DuplicatedCommonEndpointPathValidationTest extends AnyFunSuite with Matchers {

  val validation: CustomShaclValidator.CustomShaclFunction = DuplicatedCommonEndpointPathValidation()

  private def hasDuplicatePaths(api: WebApi): Boolean = {
    var hasDuplicates = false
    validation.run(api, { _ => hasDuplicates = true })
    hasDuplicates
  }

  test("Same path") {
    val endPoint             = EndPoint().withPath("/user")
    val endPointWithSamePath = EndPoint().withPath("/user")
    val api                  = WebApi().withEndPoints(Seq(endPoint, endPointWithSamePath))
    hasDuplicatePaths(api) shouldBe true
  }

  test("With parameter") {
    val endPoint          = EndPoint().withPath("/user")
    val endPointWithParam = EndPoint().withPath("/user{param}")
    val api               = WebApi().withEndPoints(Seq(endPoint, endPointWithParam))
    hasDuplicatePaths(api) shouldBe false
  }

  test("Different hierarchy") {
    val endPoint          = EndPoint().withPath("/userJorge")
    val endPointWithParam = EndPoint().withPath("/user{param}")
    val api               = WebApi().withEndPoints(Seq(endPoint, endPointWithParam))
    hasDuplicatePaths(api) shouldBe false
  }

  test("Same hierarchy") {
    val endPointWithParam1 = EndPoint().withPath("/user{param1}")
    val endPointWithParam2 = EndPoint().withPath("/user{param2}")
    val api                = WebApi().withEndPoints(Seq(endPointWithParam1, endPointWithParam2))
    hasDuplicatePaths(api) shouldBe false
  }

  test("Trailing slash") {
    val endPoint    = EndPoint().withPath("/user")
    val endPointDir = EndPoint().withPath("/user/")
    val api         = WebApi().withEndPoints(Seq(endPoint, endPointDir))
    hasDuplicatePaths(api) shouldBe false
  }

  test("Different number of parameters") {
    val endPointWithParam     = EndPoint().withPath("/user/{param1}/{param2}")
    val endPointWithTwoParams = EndPoint().withPath("/user/{param1}")
    val api                   = WebApi().withEndPoints(Seq(endPointWithTwoParams, endPointWithParam))
    hasDuplicatePaths(api) shouldBe false
  }
}
