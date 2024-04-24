package amf.xml.client.scala

import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.core.internal.plugins.syntax.SyamlSyntaxParsePlugin
import amf.xml.internal.plugins.parse.XMLParsePlugin
import amf.xml.internal.plugins.syntax.XMLSyntaxParsePlugin

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


object XMLConfiguration extends APIConfigurationBuilder {

  def XML(): AMFConfiguration =
    common()
      .withPlugins(List(XMLParsePlugin, XMLSyntaxParsePlugin))
      .withPlugin(SyamlForJsonLDSyntaxParsePlugin) // override SYAML
}
