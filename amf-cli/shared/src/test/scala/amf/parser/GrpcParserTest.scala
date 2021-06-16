package amf.parser

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.remote.{Amf, GrpcProtoHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

class GrpcParserTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/grpc/"

  multiGoldenTest("HERE_HERE Can generate simple gRPC specs", "simple.%s") { config =>
    cycle(
      "simple.proto",
      config.golden,
      GrpcProtoHint,
      Amf,
      renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint),
      eh = Some(UnhandledErrorHandler)
    )
  }
}
