package amf.parser

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.GraphQLProfile
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.unsafe.PlatformSecrets
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FileAssertionTest
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class GraphQLTCKValidationTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {
  val tckPath: String  = "amf-cli/shared/src/test/resources/graphql/tck"
  val apisPath: String = s"$tckPath/apis"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  // Test valid APIs
  fs.syncFile(s"$apisPath/valid").list.foreach { api =>
    ignore(s"GraphQL TCK > Apis > Valid > $api: should conform") { assertConforms(s"$apisPath/valid/$api") }
  }

  // Test invalid APIs
  fs.syncFile(s"$apisPath/invalid")
    .list
    .groupBy(apiName)
    .values
    .collect {
      case toValidate if toValidate.length > 1 =>
        apiName(toValidate.head) // contains the API and it's report, thus should be validated
    }
    .foreach { api =>
      test(s"GraphQL TCK > Apis > Invalid > $api: should not conform") {
        assertReport(s"$apisPath/invalid/$api.graphql")
      }
    }

  // Test singular API
  test("directive-argument-missing-value") {
    assertReport(s"$apisPath/invalid/directive-argument-missing-value.api.graphql")
  }

  def assertConforms(api: String): Future[Assertion] = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    for {
      parsing        <- client.parse(s"file://$api")
      transformation <- Future.successful(client.transform(parsing.baseUnit, PipelineId.Cache))
      validation     <- client.validate(transformation.baseUnit)
    } yield {
      assert(parsing.conforms && transformation.conforms && validation.conforms)
    }
  }

  def assertReport(api: String): Future[Assertion] = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    val apiUri = s"file://$api"
    val report = api.replace(".graphql", ".report")
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
