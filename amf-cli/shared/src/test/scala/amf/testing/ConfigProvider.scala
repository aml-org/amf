package amf.testing

import amf.apicontract.client.scala._
import amf.core.internal.remote._
import amf.graphql.client.scala.GraphQLConfiguration
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import amf.grpc.client.scala.GRPCConfiguration

object ConfigProvider {

  def configFor(spec: Spec): AMFConfiguration = spec match {
    case Raml08            => RAMLConfiguration.RAML08()
    case Raml10            => RAMLConfiguration.RAML10()
    case Oas20             => OASConfiguration.OAS20()
    case Oas30             => OASConfiguration.OAS30()
    case Oas31             => OASConfiguration.OAS31()
    case AsyncApi20        => AsyncAPIConfiguration.Async20()
    case AsyncApi21        => AsyncAPIConfiguration.Async20()
    case AsyncApi22        => AsyncAPIConfiguration.Async20()
    case AsyncApi23        => AsyncAPIConfiguration.Async20()
    case AsyncApi24        => AsyncAPIConfiguration.Async20()
    case AsyncApi25        => AsyncAPIConfiguration.Async20()
    case AsyncApi26        => AsyncAPIConfiguration.Async20()
    case Grpc              => GRPCConfiguration.GRPC()
    case GraphQL           => GraphQLConfiguration.GraphQL()
    case GraphQLFederation => GraphQLFederationConfiguration.GraphQLFederation()
    case _                 => BaseApiConfiguration.BASE()
  }
}
