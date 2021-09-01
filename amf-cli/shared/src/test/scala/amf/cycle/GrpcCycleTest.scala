package amf.cycle

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.GrpcProtoHint
import amf.grpc.client.scala.GRPCConfiguration
import amf.io.FunSuiteCycleTests

import scala.concurrent.Future

class GrpcCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/grpc/"

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = GRPCConfiguration.GRPC()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
  }

  test("Can cycle through a simple gRPC API") {
    cycle("simple/api.proto", "simple/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }
}
