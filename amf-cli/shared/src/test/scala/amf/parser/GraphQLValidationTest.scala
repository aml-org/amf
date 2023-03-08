package amf.parser

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.GraphQLProfile
import amf.core.client.scala.validation.AMFValidationReport
import amf.cycle.GraphQLFunSuiteCycleTests
import amf.graphql.client.scala.GraphQLConfiguration
import org.scalatest.Assertion

import scala.concurrent.Future

trait GraphQLValidationTest extends GraphQLFunSuiteCycleTests {


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

}
