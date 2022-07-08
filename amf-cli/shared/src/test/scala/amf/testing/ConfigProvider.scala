package amf.testing

import amf.apicontract.client.scala._
import amf.core.internal.remote._
import amf.graphql.client.scala.{GraphQLConfiguration, GraphQLFederationConfiguration}
import amf.grpc.client.scala.GRPCConfiguration

object ConfigProvider {

  def configFor(spec: Spec): AMFConfiguration = spec match {
    case Raml08            => RAMLConfiguration.RAML08()
    case Raml10            => RAMLConfiguration.RAML10()
    case Oas20             => OASConfiguration.OAS20()
    case Oas30             => OASConfiguration.OAS30()
    case AsyncApi20        => AsyncAPIConfiguration.Async20()
    case Grpc              => GRPCConfiguration.GRPC()
    case GraphQL           => GraphQLConfiguration.GraphQL()
    case GraphQLFederation => GraphQLFederationConfiguration.GraphQLFederation()
    case _                 => BaseApiConfiguration.BASE()
  }
}
