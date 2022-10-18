package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.GraphQLFederationHint
import amf.core.internal.remote.Mimes.`application/graphql`
import amf.graphql.client.scala.GraphQLConfiguration
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration

import scala.concurrent.Future

class GraphQLFederationTCKIntrospectionTest extends GraphQLFederationFunSuiteCycleTests {
  override def basePath: String = s"amf-cli/shared/src/test/resources/graphql-federation/tck/apis/valid/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".graphql") && !api.endsWith(".dumped.graphql")) {
      test(s"GraphQL Federation TCK > Apis > Valid > $api: introspected GraphQL matches golden") {
        cycle(api, api.replace(".graphql", ".dumped.graphql"), GraphQLFederationHint, GraphQLFederationHint)
      }

      test(s"GraphQL Federation TCK > Apis > Valid > $api: introspected GraphQL should be valid") {
        val fedClient     = GraphQLFederationConfiguration.GraphQLFederation().baseUnitClient()
        val graphqlClient = GraphQLConfiguration.GraphQL().baseUnitClient()
        for {
          parsing       <- fedClient.parse(s"file://$basePath/$api")
          introspection <- Future.successful(fedClient.transform(parsing.baseUnit, PipelineId.Introspection))
          report        <- graphqlClient.validate(introspection.baseUnit)
        } yield {
          if (!report.conforms) {
            println(report.toString)
          }
          assert(report.conforms)
        }
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
