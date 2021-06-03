package amf.plugins.document.apicontract.validation.runtimeexpression

import scala.util.matching.Regex

trait ExpressionValidator {

  /**
    * validates text that is an expression, or combination of static text with embedded expressions.
    */
  def validate(text: String): Boolean =
    if (text.startsWith("$"))
      expression(text)
    else {
      val expressions: Regex.MatchIterator = "\\{.*?\\}".r.findAllIn(text)
      expressions.forall(exp => expression(exp.init.tail))
    }

  /**
    * validates if the expression is valid, as defined by the corresponding ABNF syntax.
    */
  def expression(exp: String): Boolean
}

object Oas3ExpressionValidator extends ExpressionValidator {

  def expression(exp: String): Boolean = OAS3RuntimeExpressionParser(exp).completelyValid
}

object AsyncExpressionValidator extends ExpressionValidator {

  override def expression(exp: String): Boolean = AsyncAPIRuntimeExpressionParser(exp).completelyValid
}
