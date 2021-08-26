package amf.graphql.client.scala

import amf.antlr.internal.plugins.syntax.AntlrSyntaxParsePlugin
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.graphql.plugins.parse.GraphQLParsePlugin

object GraphQLConfiguration extends APIConfigurationBuilder {

  def GraphQL(): AMFConfiguration = {
    common().withPlugins(List(GraphQLParsePlugin, AntlrSyntaxParsePlugin))
  }
}
