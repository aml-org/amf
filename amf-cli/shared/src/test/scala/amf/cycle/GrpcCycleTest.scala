package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, GrpcProtoHint}
import amf.grpc.client.scala.GRPCConfiguration
import amf.io.FunSuiteCycleTests

class GrpcCycleTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/grpc/"

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    GRPCConfiguration
      .GRPC()
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(UnhandledErrorHandler))
  }

  test("Can cycle through a simple gRPC API") {
    cycle("simple/api.proto", "simple/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }

  test("GRPC reserved keyword emission") {
    cycle("reserved/api.proto", "reserved/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }

  test("GRPC Google Protobuf well-known imports emission") {
    cycle("imports/api.proto", "imports/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }

  test("GRPC extensions emission") {
    cycle("extensions/all.proto", "extensions/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }

  test("GRPC complete example to proto") {
    cycle("all/main.proto", "all/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }

  test("GRPC complete example to JSON-LD") {
    cycle("all/main.proto", "all/dumped.jsonld", GrpcProtoHint, AmfJsonHint)
  }
}
