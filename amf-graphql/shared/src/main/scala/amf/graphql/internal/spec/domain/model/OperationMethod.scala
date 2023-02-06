package amf.graphql.internal.spec.domain.model

import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes

object OperationMethod {

  def apply(operationType: RootTypes.Value): String = operationType match {
    case RootTypes.Query        => "query"
    case RootTypes.Mutation     => "post"
    case RootTypes.Subscription => "subscribe"
  }
}
