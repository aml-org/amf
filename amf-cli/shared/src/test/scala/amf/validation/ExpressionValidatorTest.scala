package amf.validation

import amf.plugins.document.apicontract.validation.runtimeexpression.{AsyncExpressionValidator, ExpressionValidator, Oas3ExpressionValidator}
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

  val asyncExpressions: Seq[ExpressionTest] = Seq(
    ExpressionTest("$message.header#/hello", expected = true),
    ExpressionTest("$message.payload#/hello", expected = true),
    ExpressionTest("$method", expected = false),
    ExpressionTest("$method.name", expected = false),
    ExpressionTest("$url", expected = false),
    ExpressionTest("$url.extension", expected = false),
    ExpressionTest("$statusCode", expected = false),
    ExpressionTest("$response.header.Server", expected = false),
  )

  validateExpressions(oas3Expressions, Oas3ExpressionValidator, "OAS 3.0")
  validateExpressions(asyncExpressions, AsyncExpressionValidator, "Async 2.0")

  def validateExpressions(tests: Seq[ExpressionTest], validator: ExpressionValidator, specLabel: String): Unit = {
    tests.foreach {
      case ExpressionTest(text, expected) =>
        test(s"$specLabel - '$text' validation should be $expected") {
          validator.validate(text) should be(expected)
        }
    }
  }

  case class ExpressionTest(text: String, expected: Boolean)
}
