package amf.parser

class GraphQLTCKValidationTest extends GraphQLValidationTest {
  override def basePath: String = "amf-cli/shared/src/test/resources/graphql/tck/apis"

  // Test valid APIs
  fs.syncFile(s"$basePath/valid").list.foreach { api =>
    ignore(s"GraphQL TCK > Apis > Valid > $api: should conform") { assertConforms(s"$basePath/valid/$api") }
  }

  // Test invalid APIs
  fs.syncFile(s"$basePath/invalid")
    .list
    .groupBy(apiName)
    .values
    .collect {
      case toValidate if toValidate.length > 1 =>
        apiName(toValidate.head) // contains the API and it's report, thus should be validated
    }
    .foreach { api =>
      test(s"GraphQL TCK > Apis > Invalid > $api: should not conform") {
        assertReport(s"$basePath/invalid/$api.graphql")
      }
    }

  // Test invalid singular API
  ignore("interface-chain") {
    assertReport(s"$basePath/invalid/missing-matrix-field.graphql")
  }

  // Test valid singular API. Only used to debug a Singular test, this is run as part of the TCK
  ignore("interface-chain-covariant") {
    assertConforms(s"$basePath/valid/interface-chain-covariant.graphql")
  }

  private def apiName(api: String): String = api.split('.').dropRight(1).mkString(".")
}
