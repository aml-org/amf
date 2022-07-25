package amf.graphql

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, GraphQLFederationHint}
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import amf.io.FunSuiteCycleTests

trait GraphQLFederationFunSuiteCycleTests extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = GraphQLFederationConfiguration.GraphQLFederation()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e)
    )
  }

  override def renderOptions(): RenderOptions = super.renderOptions().withPrettyPrint
}

class GraphQLFederationTCKParsingTest extends GraphQLFederationFunSuiteCycleTests {
  override def basePath: String = s"amf-graphql-test-sets/shared/src/test/resources/graphql-federation/tck/apis/valid/"

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
