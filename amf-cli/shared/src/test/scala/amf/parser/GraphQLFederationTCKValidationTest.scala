package amf.parser

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.GraphQLProfile
import amf.core.client.scala.validation.AMFValidationReport
import amf.graphql.client.scala.GraphQLConfiguration
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class GraphQLFederationTCKValidationTest extends GraphQLFederationFunSuiteCycleTests {

  val graphqlTckPath: String  = "amf-cli/shared/src/test/resources/graphql/tck"
  val graphqlApisPath: String = s"$graphqlTckPath/apis"

  private val GRAPHQL    = "GRAPHQL VANILLA"
  private val FEDERATION = "GRAPHQL FEDERATION"

  private val ignoredApis = Set(
    "amf-cli/shared/src/test/resources/graphql/tck/apis/invalid/directive-argument-wrong-value.api.graphql",
    "amf-cli/shared/src/test/resources/graphql/tck/apis/invalid/duplicate-interfaces-object.api.graphql",
    "amf-cli/shared/src/test/resources/graphql/tck/apis/invalid/recursive-directive-direct.graphql",
    "amf-cli/shared/src/test/resources/graphql-federation/tck/apis//invalid/non-external-provides-nested.graphql",
    "amf-cli/shared/src/test/resources/graphql-federation/tck/apis//invalid/non-external-provides.graphql"
  )

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

  private def runInvalidGraphqlTck() =
    runInvalidTests(graphqlApisPath, GRAPHQL, _.replace(".graphql", ".federation.report"))
  private def runInvalidFederationTck() =
    runInvalidTests(basePath, FEDERATION, _.replace(".graphql", ".report"))

  private def runValidGraphqlTck()    = runValidTests(graphqlApisPath, GRAPHQL)
  private def runValidFederationTck() = runValidTests(basePath, FEDERATION)

  private def runValidTests(basePath: String, testNamePrefix: String) = {
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

  private def runInvalidTests(base: String, testNamePrefix: String, reportProducer: String => String) = {
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
        else if (ignoredApis.contains(finalPath)) {
          ignore(s"$testNamePrefix TCK > Apis > Invalid > $api: should not conform") {
            val report = reportProducer(finalPath)
            assertReport(finalPath, report)
          }
        } else {
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

  override def basePath: String = s"amf-cli/shared/src/test/resources/graphql-federation/tck/apis/"
}
