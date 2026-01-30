package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.GraphQLProfile
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.client.scala.validation.AMFValidationReport
import amf.graphql.client.scala.GraphQLConfiguration
import amf.grpc.client.scala.GRPCConfiguration
import amf.io.FunSuiteCycleTests
import org.scalatest.Assertion

import scala.concurrent.Future

trait GrpcFunSuiteCycleTest extends FunSuiteCycleTests {

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    GRPCConfiguration
      .GRPC()
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(IgnoringErrorHandler))
  }

  def assertReport(api: String): Future[Assertion] = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    val apiUri = s"file://$api"
    val report = api.replace(".proto", ".report")
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
