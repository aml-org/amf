package amf.antlr.internal.plugins.syntax

import amf.core.internal.plugins.syntax.SyamlSyntaxParsePlugin

object SyamlForJsonLDSyntaxParsePlugin extends SyamlSyntaxParsePlugin {

  /** Need to override applies method because GraphQL, GraphQL Federation & GRPC syntax plugins return applies true for
    * ALL strings, same as SYAML. We need some sort of syntax detection otherwise we will not be able to parse GraphQL,
    * GRPC, etc. and JSON-LD in the same configuration. Doing that syntax detection here
    * @param element
    *   input
    * @return
    */
  override def applies(element: CharSequence): Boolean = {
    val text = element.toString.trim
    text.nonEmpty && (text.charAt(0) match {
      case '{' => true
      case '[' => true
      case _   => false
    })
  }
}
