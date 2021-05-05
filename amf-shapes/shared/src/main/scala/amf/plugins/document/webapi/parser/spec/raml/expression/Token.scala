package amf.plugins.document.webapi.parser.spec.raml.expression

import amf.core.annotations.LexicalInformation

private[expression] object Token {
  val START_GROUP = "START_GROUP"
  val END_GROUP   = "END_GROUP"
  val START_ARRAY = "START_ARRAY"
  val END_ARRAY   = "END_ARRAY"
  val UNION       = "UNION"
  val SYMBOL      = "SYMBOL"
  val WHITESPACE  = "WHITESPACE"
}

private[expression] case class Token(token: String, value: String, lexicalInformation: LexicalInformation)
