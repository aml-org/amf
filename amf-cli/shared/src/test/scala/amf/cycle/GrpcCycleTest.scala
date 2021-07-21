package amf.cycle

import amf.core.internal.remote.GrpcProtoHint
import amf.core.internal.remote.Vendor.PROTO3
import amf.io.FunSuiteCycleTests

class GrpcCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/grpc/"

  test("Can cycle through a simple gRPC API") {
    cycle("simple/api.proto",
      "simple/dumped.proto",
      GrpcProtoHint,
      PROTO3)
  }
}
