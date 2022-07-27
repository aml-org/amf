package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.internal.remote.{GraphQLHint, Hint}
import amf.graphql.client.scala.GraphQLConfiguration
import org.scalatest.matchers.should.Matchers

class GraphQLUniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/graphql/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/graphql/"

  val config: AMFConfiguration = GraphQLConfiguration.GraphQL()

  test("Can parse API with missing EOF") {
    validate("sudden-eof.graphql", Some("sudden-eof.report"), configOverride = Some(config))
  }

  test("validate types in default values in directives") {
    validate("invalid-default-value-directive.graphql", Some("invalid-default-value-directive.report"), configOverride = Some(config))
  }

  test("validate types in default values in parameters") {
    validate("invalid-default-value-parameters.graphql", Some("invalid-default-value-parameters.report"), configOverride = Some(config))
  }

  test("validate enums in default values") {
    validate("invalid-default-value-enum.graphql", Some("invalid-default-value-enum.report"), configOverride = Some(config))
  }
}
