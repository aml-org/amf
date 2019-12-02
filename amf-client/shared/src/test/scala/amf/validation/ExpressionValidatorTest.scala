package amf.validation

import amf.plugins.document.webapi.validation.Oas3ExpressionValidator
import org.scalatest.{FunSuite, Matchers}

class ExpressionValidatorTest extends FunSuite with Matchers {

  val oas3Expressions: Seq[ExpressionTest] = Seq(
    ExpressionTest("server.com?id={$request.body#/id}&mail={$request.body}", expected = true),
    ExpressionTest("{$request.body#/callbackUrl}", expected = true),
    ExpressionTest("$request.body#/callbackUrl", expected = true),
    ExpressionTest("$method", expected = true),
    ExpressionTest("$method.name", expected = false),
    ExpressionTest("$url", expected = true),
    ExpressionTest("$url.extension", expected = false),
    ExpressionTest("$statusCode", expected = true),
    ExpressionTest("$statusCode.code", expected = false),
    ExpressionTest("$response.header.Server", expected = true),
    ExpressionTest("$response.query.Server", expected = true),
    ExpressionTest("$request.path.id", expected = true)
  )

  oas3Expressions.foreach {
    case ExpressionTest(text, expected) =>
      test(s"OAS 3.0 expression '$text' validation should be $expected") {
        Oas3ExpressionValidator.validate(text) should be(expected)
      }
  }

  case class ExpressionTest(text: String, expected: Boolean)
}
