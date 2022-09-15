package amf.parser

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.GraphQLProfile
import amf.core.client.scala.validation.AMFValidationReport
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class GraphQLFederationTCKValidationTest extends GraphQLFederationFunSuiteCycleTests {
  override def basePath: String = s"amf-cli/shared/src/test/resources/graphql-federation/tck/apis/"

  val graphqlTckPath: String  = "amf-cli/shared/src/test/resources/graphql/tck"
  val graphqlApisPath: String = s"$graphqlTckPath/apis"

  private val GRAPHQL    = "GRAPHQL VANILLA"
  private val FEDERATION = "GRAPHQL FEDERATION"

  private val validFederationApisInvalidInGraphQL = Set(
    "amf-cli/shared/src/test/resources/graphql/tck/apis/invalid/mandatory-schema-node.api.graphql"
  )

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  // Test valid APIs
  runValidGraphqlTck()
  runValidFederationTck()

  // Test invalid APIs

  runInvalidGraphqlTck()
  runInvalidFederationTck()

  private val apiPath =
    "amf-cli/shared/src/test/resources/graphql-federation/tck/apis//invalid/invalid-key-directive.graphql"

  // Test singular API
  test("invalid-key-directive") {
    assertReport(apiPath, apiPath.replace(".graphql", ".report"))
  }

  private def runInvalidGraphqlTck(): Unit =
    runInvalidTests(graphqlApisPath, GRAPHQL, _.replace(".graphql", ".federation.report"))
  private def runInvalidFederationTck(): Unit =
    runInvalidTests(basePath, FEDERATION, _.replace(".graphql", ".report"))

  private def runValidGraphqlTck(): Unit    = runValidTests(graphqlApisPath, GRAPHQL)
  private def runValidFederationTck(): Unit = runValidTests(basePath, FEDERATION)

  private def runValidTests(basePath: String, testNamePrefix: String): Unit = {
    fs.syncFile(s"$basePath/valid").list.foreach { api =>
      ignore(s"$testNamePrefix TCK > Apis > Valid > $api: should conform") {
        assertConforms(s"$basePath/valid/$api")
      }
    }
    validFederationApisInvalidInGraphQL.foreach { api =>
      ignore(s"$testNamePrefix TCK > Apis > Valid > $api: should conform") {
        assertConforms(api)
      }
    }
  }

  private def runInvalidTests(base: String, testNamePrefix: String, reportProducer: String => String): Unit = {
    fs.syncFile(s"$base/invalid")
      .list
      .groupBy(apiName)
      .values
      .collect {
        case toValidate if toValidate.length > 1 =>
          apiName(toValidate.head) // contains the API and it's report, thus should be validated
      }
      .foreach { api =>
        val finalPath = s"$base/invalid/$api.graphql"
        if (validFederationApisInvalidInGraphQL.contains(finalPath)) {} // ignore
        else {
          test(s"$testNamePrefix TCK > Apis > Invalid > $api: should not conform") {
            val report = reportProducer(finalPath)
            assertReport(finalPath, report)
          }
        }
      }
  }

  def assertConforms(api: String): Future[Assertion] = {
    val client = GraphQLFederationConfiguration.GraphQLFederation().baseUnitClient()
    for {
      parsing        <- client.parse(s"file://$api")
      transformation <- Future.successful(client.transform(parsing.baseUnit, PipelineId.Cache))
      validation     <- client.validate(transformation.baseUnit)
    } yield {
      assert(parsing.conforms && transformation.conforms && validation.conforms)
    }
  }

  def assertReport(api: String, report: String): Future[Assertion] = {
    val client = GraphQLFederationConfiguration.GraphQLFederation().baseUnitClient()
    val apiUri = s"file://$api"
    for {
      parsing        <- client.parse(apiUri)
      transformation <- Future.successful(client.transform(parsing.baseUnit, PipelineId.Cache))
      validation     <- client.validate(transformation.baseUnit)
      actualFile <- {
        val combinedResults = parsing.results ++ transformation.results ++ validation.results
        val actual          = AMFValidationReport(apiUri, GraphQLProfile, combinedResults)
        writeTemporaryFile(report)(actual.toString)
      }
      assertion <- assertDifferences(actualFile, report)
    } yield {
      assertion
    }
  }

  private def apiName(api: String): String = api.split('.').dropRight(1).mkString(".")
}
