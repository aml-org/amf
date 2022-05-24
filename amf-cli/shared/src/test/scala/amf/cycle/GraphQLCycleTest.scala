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
      renderedConfig.withErrorHandlerProvider(() => e)
    )
  }

  override def renderOptions(): RenderOptions = super.renderOptions().withPrettyPrint
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

  test("Can parse API with keywords as names") {
    cycle("keyword-names/api.graphql", "keyword-names/api.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse root type API") {
    cycle("root-type/root-type.graphql", "root-type/root-type.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse non root type API") {
    cycle("root-type/non-root-type.graphql", "root-type/non-root-type.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse arguments with default values") {
    cycle(
      "default-arguments/default-arguments.graphql",
      "default-arguments/default-arguments.jsonld",
      GraphQLHint,
      AmfJsonHint
    )
  }

  test("Can parse arguments with incorrect default values") {
    cycle(
      "default-arguments/invalid-default-arguments.graphql",
      "default-arguments/invalid-default-arguments.jsonld",
      GraphQLHint,
      AmfJsonHint
    )
  }

  test("Can parse API with root level non-optional arrays") {
    cycle(
      "non-root-optional-array/api.graphql",
      "non-root-optional-array/api.jsonld",
      GraphQLHint,
      AmfJsonHint
    )
  }

  test("Can parse API with 'fragment' field name") {
    cycle(
      "fragment-reserved-name/api.graphql",
      "fragment-reserved-name/api.jsonld",
      GraphQLHint,
      AmfJsonHint
    )
  }

  test("Can parse API with keyword enum values") {
    cycle(
      "keyword-enum-values/api.graphql",
      "keyword-enum-values/api.jsonld",
      GraphQLHint,
      AmfJsonHint
    )
  }

}
