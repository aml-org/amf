package amf.graphql

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}

class GraphQLTCKParsingTest extends GraphQLFunSuiteCycleTests {
  override def basePath: String = s"amf-graphql-test-sets/shared/src/test/resources/graphql/tck/apis/valid/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".graphql") && !api.endsWith(".dumped.graphql")) {
      test(s"GraphQL TCK > Apis > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".graphql", ".jsonld"), GraphQLHint, AmfJsonHint)
      }

      ignore(s"GraphQL TCK > Apis > Valid > $api: dumped GraphQL matches golden") {
        cycle(api, api.replace(".graphql", ".dumped.graphql"), GraphQLHint, GraphQLHint)
      }
    }
  }

  ignore("specific graphql api") {
    val api = "directive-arguments-default.api.graphql"
    cycle(api, api.replace(".graphql", ".jsonld"), GraphQLHint, AmfJsonHint)
  }

  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
}
