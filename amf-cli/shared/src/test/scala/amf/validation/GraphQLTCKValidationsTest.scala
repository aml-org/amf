package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.internal.remote.{GraphQLHint, Hint}
import amf.graphql.client.scala.GraphQLConfiguration
import org.scalatest.matchers.should.Matchers

class GraphQLTCKValidationsTest extends UniquePlatformReportGenTest with Matchers {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/graphql/tck/apis/invalid/"
  override val reportsPath: String = "file://amf-cli/shared/src/test/resources/graphql/tck/apis/invalid/"
  override val hint: Hint          = GraphQLHint

  val config: AMFConfiguration = GraphQLConfiguration.GraphQL()

  test("directive-argument-wrong-value") {
    validate(
      "directive-argument-wrong-value.api.graphql",
      Some("directive-argument-wrong-value.api.report"),
      configOverride = Some(config)
    )
  }

}
