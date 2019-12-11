package amf.plugins.document.webapi.validation

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

  def expression(exp: String): Boolean = exp match {
    case "$url" | "$method" | "$statusCode" => true
    case _ if exp.startsWith("$request.")   => source(exp.replaceFirst("\\$request.", ""))
    case _ if exp.startsWith("$response.")  => source(exp.replaceFirst("\\$response.", ""))
    case _                                  => false
  }

  def source(exp: String): Boolean = headerRef(exp) || queryRef(exp) || pathRef(exp) || bodyRef(exp)

  def headerRef(exp: String): Boolean = exp.startsWith("header.") // token is not validated

  def queryRef(exp: String): Boolean = exp.startsWith("query.") // name is not validated

  def pathRef(exp: String): Boolean = exp.startsWith("path.") // name is not validated

  def bodyRef(exp: String): Boolean = exp.startsWith("body") && fragment(exp.replaceFirst("body", ""))

  def fragment(str: String): Boolean = str match {
    case ""                        => true
    case _ if str.startsWith("#/") => true
    case _                         => false
  }
}
