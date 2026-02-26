package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.errorhandling.IgnoringErrorHandler
import amf.core.client.scala.model.document.Document
import amf.grpc.client.scala.GRPCConfiguration

class GrpcParserAssertionTest extends GrpcFunSuiteCycleTests {
  override def basePath: String = "file://amf-cli/shared/src/test/resources/upanddown/grpc/"

  private val configuration: AMFConfiguration =
    GRPCConfiguration.GRPC().withErrorHandlerProvider(() => IgnoringErrorHandler)

  test("Can parse gRPC spec") {
    for {
      parsingResult <- configuration
        .baseUnitClient()
        .parse(basePath + "lexicals/message-lexicals/message-lexicals.proto")
    } yield {
      assert(parsingResult.conforms)
      val unit = parsingResult.baseUnit
      val operation = unit
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[WebApi]
        .endPoints
        .head
        .operations
        .head
      val requestPayload  = operation.request.payloads.head
      val responsePayload = operation.responses.head.payloads.head

      assert(requestPayload.annotations.lexical().toString == "[(9,14)-(9,25)]")
      assert(responsePayload.annotations.lexical().toString == "[(9,36)-(9,50)]")
    }
  }
}
