package amf.parser

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, GraphQLFederationHint}

class GraphQLFederationTCKParsingTest extends GraphQLFederationFunSuiteCycleTests {
  override def basePath: String = s"amf-cli/shared/src/test/resources/graphql-federation/tck/apis/valid/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".graphql") && !api.endsWith(".dumped.graphql")) {
      test(s"GraphQL Federation TCK > Apis > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".graphql", ".jsonld"), GraphQLFederationHint, AmfJsonHint)
      }

      ignore(s"GraphQL Federation TCK > Apis > Valid > $api: dumped GraphQL matches golden") {
        cycle(api, api.replace(".graphql", ".dumped.graphql"), GraphQLFederationHint, GraphQLFederationHint)
      }
    }
  }

  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
}
