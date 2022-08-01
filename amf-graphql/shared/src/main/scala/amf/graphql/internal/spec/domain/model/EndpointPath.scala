package amf.graphql.internal.spec.domain.model

import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes

object EndpointPath {

  def apply(fieldName: String, operationType: RootTypes.Value): String = operationType match {
    case RootTypes.Query        => s"/query/$fieldName"
    case RootTypes.Mutation     => s"/mutation/$fieldName"
    case RootTypes.Subscription => s"/subscription/$fieldName"
  }
}
