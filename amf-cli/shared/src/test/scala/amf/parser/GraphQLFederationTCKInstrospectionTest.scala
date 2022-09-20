package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.GraphQLFederationHint
import amf.core.internal.remote.Mimes.`application/graphql`
import amf.graphql.client.scala.GraphQLConfiguration

class GraphQLFederationTCKInstrospectionTest extends GraphQLFederationFunSuiteCycleTests {
  override def basePath: String = s"amf-cli/shared/src/test/resources/graphql-federation/tck/apis/valid/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".graphql") && !api.endsWith(".dumped.graphql")) {
      test(s"GraphQL Federation TCK > Apis > Valid > $api: introspected GraphQL matches golden") {
        cycle(api, api.replace(".graphql", ".dumped.graphql"), GraphQLFederationHint, GraphQLFederationHint)
      }
    }
  }

  /** Method to render parsed unit. Override if necessary. */
  override def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): String = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    client.render(unit, `application/graphql`)
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    val client              = amfConfig.baseUnitClient()
    val introspectionResult = client.transform(unit, PipelineId.Introspection)
    introspectionResult.baseUnit
  }
}
