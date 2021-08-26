package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint, GrpcProtoHint}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FunSuiteCycleTests

class GraphQLCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/graphql/"

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = GraphQLConfiguration.GraphQL()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
  }

  test("Can cycle through a simple gRPC API") {
    cycle("simple/api.graphql", "simple/api.jsonld", GraphQLHint, AmfJsonHint)
  }

}
