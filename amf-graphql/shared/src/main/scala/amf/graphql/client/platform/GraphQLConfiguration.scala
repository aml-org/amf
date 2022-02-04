package amf.graphql.client.platform

import amf.apicontract.client.platform.AMFConfiguration
import amf.graphql.client.scala.{GraphQLConfiguration => InternalGraphQLConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("GraphQLConfiguration")
object GraphQLConfiguration {

  def GraphQL(): AMFConfiguration = {
    new AMFConfiguration(InternalGraphQLConfiguration.GraphQL())
  }
}
