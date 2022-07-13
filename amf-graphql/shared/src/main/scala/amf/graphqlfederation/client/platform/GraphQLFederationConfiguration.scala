package amf.graphqlfederation.client.platform

import amf.apicontract.client.platform.AMFConfiguration
import amf.graphqlfederation.client.scala.{GraphQLFederationConfiguration => InternalGraphQLFederationConfiguration}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("GraphQLFederationConfiguration")
object GraphQLFederationConfiguration {

  def GraphQLFederation(): AMFConfiguration = {
    new AMFConfiguration(InternalGraphQLFederationConfiguration.GraphQLFederation())
  }
}
