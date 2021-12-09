package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FunSuiteCycleTests

trait GraphQLFunSuiteCycleTests extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = GraphQLConfiguration.GraphQL()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
  }

}


class GraphQLCycleTest extends GraphQLFunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/graphql/"


  test("Can parse a simple GraphQL API") {
    cycle("simple/api.graphql", "simple/api.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can cycle through a simple GraphQL API") {
    cycle("simple/api.graphql", "simple/dumped.graphql", GraphQLHint, GraphQLHint)
  }

  test("Can parse the SWAPI GraphQL API") {
    cycle("swapi/api.graphql", "swapi/api.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can cycle through the SWAPI GraphQL API") {
    cycle("swapi/api.graphql", "swapi/dumped.graphql", GraphQLHint, GraphQLHint)
  }

}