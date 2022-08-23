package amf.parser

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}
import amf.cycle.GraphQLFunSuiteCycleTests

class GraphQLTCKParsingTest extends GraphQLFunSuiteCycleTests {
  override def basePath: String = s"amf-cli/shared/src/test/resources/graphql/tck/apis/valid/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".graphql") && !api.endsWith(".dumped.graphql")) {
      test(s"GraphQL TCK > Apis > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".graphql", ".jsonld"), GraphQLHint, AmfJsonHint)
      }

      test(s"GraphQL TCK > Apis > Valid > $api: dumped GraphQL matches golden") {
        cycle(api, api.replace(".graphql", ".dumped.graphql"), GraphQLHint, GraphQLHint)
      }
    }
  }

  ignore("specific graphql api dumped JSON matches golden") {
    val api = "directive-arguments-default.api.graphql"
    cycle(api, api.replace(".graphql", ".jsonld"), GraphQLHint, AmfJsonHint)
  }

  ignore("specific graphql api dumped GraphQL matches golden") {
    val api = "extension-union.api.graphql"
    cycle(api, api.replace(".graphql", ".dumped.graphql"), GraphQLHint, GraphQLHint)
  }

  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
}
