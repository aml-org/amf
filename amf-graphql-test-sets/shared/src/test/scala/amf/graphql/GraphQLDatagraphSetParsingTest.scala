package amf.graphql

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}

class GraphQLDatagraphSetParsingTest extends GraphQLFunSuiteCycleTests {
  override def basePath: String = s"amf-graphql-test-sets/shared/src/test/resources/graphql/datagraph-set/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { directory =>
    val api           = s"$directory/api.graphql"
    val dumpedJsonLd  = s"$directory/api.jsonld"
    val dumpedGraphQL = s"$directory/api.dumped.graphql"
    test(s"Datagraph Set > $directory: dumped JSON matches golden") {
      cycle(api, dumpedJsonLd, GraphQLHint, AmfJsonHint)
    }

    ignore(s"Datagraph Set > $directory: dumped GraphQL matches golden") {
      cycle(api, dumpedGraphQL, GraphQLHint, GraphQLHint)
    }
  }

  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
}
