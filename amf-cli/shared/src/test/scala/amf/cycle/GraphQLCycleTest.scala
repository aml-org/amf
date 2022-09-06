package amf.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}

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

  test("Can parse API with simple directive applications") {
    cycle("directives/simple.graphql", "directives/simple.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse API with directive applications with arguments") {
    cycle("directives/arguments.graphql", "directives/arguments.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse API with graphql default directives") {
    cycle("directives/default.graphql", "directives/default.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse API with multiple directives in a single element") {
    cycle("directives/multiple.graphql", "directives/multiple.jsonld", GraphQLHint, AmfJsonHint)
  }

  test("Can parse APIs with simple descriptions") {
    cycle(
      "descriptions/simple.graphql",
      "descriptions/simple.jsonld",
      GraphQLHint,
      AmfJsonHint,
      renderOptions = Some(RenderOptions().withoutFlattenedJsonLd.withPrettyPrint.withSourceMaps)
    )
  }

  test("Can parse APIs with block descriptions") {
    cycle(
      "descriptions/block.graphql",
      "descriptions/block.jsonld",
      GraphQLHint,
      AmfJsonHint,
      renderOptions = Some(RenderOptions().withoutFlattenedJsonLd.withPrettyPrint.withSourceMaps)
    )
  }

  test("Can parse API with directive applications within directive declarations") {
    cycle(
      "directive-with-directives/api.graphql",
      "directive-with-directives/api.jsonld",
      GraphQLHint,
      AmfJsonHint,
      renderOptions = Some(RenderOptions().withoutFlattenedJsonLd.withPrettyPrint.withSourceMaps)
    )
  }

  test("Can parse API wit repeatable directive from Embedded JSON-LD"){
    cycle(
      "repeatable/repeatable.expanded.jsonld",
      "repeatable/repeatable.dumped.expanded.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(RenderOptions().withoutFlattenedJsonLd.withPrettyPrint.withSourceMaps)
    )
  }

  test("Can parse API wit repeatable directive from Flattened JSON-LD"){
    cycle(
      "repeatable/repeatable.flattened.jsonld",
      "repeatable/repeatable.dumped.flattened.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(RenderOptions().withFlattenedJsonLd.withPrettyPrint.withSourceMaps)
    )
  }
}
