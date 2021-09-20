package amf.sfdc.client.scala

import amf.antlr.internal.plugins.syntax.{AntlrSyntaxParsePlugin, AntlrSyntaxRenderPlugin}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.sfdc.plugins.parse.SfdcParsePlugin

object SFDCConfiguration  extends APIConfigurationBuilder {
    def SFDC(): AMFConfiguration =
      common()
        .withPlugins(List(SfdcParsePlugin))
}
