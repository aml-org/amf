package amf.parser

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.remote.{Amf, GrpcProtoHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

class GrpcParserTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/grpc/"

  multiGoldenTest("Can generate simple gRPC specs", "simple.%s") { config =>
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

class StandardGoogleProtoParserTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/grpc/google/"

  Seq("any.proto", "api.proto", "duration.proto", "empty.proto", "field_mask.proto",
    "source_context.proto", "struct.proto", "timestamp.proto", "type.proto", "wrappers.proto").foreach { protoFile =>

    multiGoldenTest(s"Can generate standard google proto file ${protoFile}", s"${protoFile}.%s") { config =>
      cycle(
        protoFile,
        config.golden,
        GrpcProtoHint,
        Amf,
        renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint),
        eh = Some(UnhandledErrorHandler)
      )
    }
  }
}