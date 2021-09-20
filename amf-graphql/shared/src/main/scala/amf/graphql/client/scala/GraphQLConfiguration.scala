package amf.graphql.client.scala

import amf.antlr.internal.plugins.syntax.{AntlrSyntaxParsePlugin, AntlrSyntaxRenderPlugin}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.graphql.plugins.parse.GraphQLParsePlugin
import amf.graphql.plugins.render.GraphQLRenderPlugin

object GraphQLConfiguration extends APIConfigurationBuilder {

  def GraphQL(): AMFConfiguration = {
    common()
      .withPlugins(List(GraphQLParsePlugin, AntlrSyntaxParsePlugin, GraphQLRenderPlugin, AntlrSyntaxRenderPlugin))
  }
}
