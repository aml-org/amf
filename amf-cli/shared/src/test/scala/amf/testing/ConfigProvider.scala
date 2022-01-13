package amf.testing

import amf.apicontract.client.scala.{AMFConfiguration, AsyncAPIConfiguration, BaseApiConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.internal.remote.{Amf, AsyncApi20, Grpc, Oas20, Oas30, Raml08, Raml10, Spec}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.graphql.client.scala.GraphQLConfiguration.GraphQL
import amf.grpc.client.scala.GRPCConfiguration

object ConfigProvider {

  def configFor(spec: Spec): AMFConfiguration = spec match {
    case Raml08     => RAMLConfiguration.RAML08()
    case Raml10     => RAMLConfiguration.RAML10()
    case Oas20      => OASConfiguration.OAS20()
    case Oas30      => OASConfiguration.OAS30()
    case AsyncApi20 => AsyncAPIConfiguration.Async20()
    case Grpc       => GRPCConfiguration.GRPC()
    case amf.core.internal.remote.GraphQL => GraphQLConfiguration.GraphQL()
    case _          => BaseApiConfiguration.BASE()
  }
}
