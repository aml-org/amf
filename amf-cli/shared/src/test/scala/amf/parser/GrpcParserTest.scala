package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler, UnhandledErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, GrpcProtoHint}
import amf.grpc.client.scala.GRPCConfiguration
import amf.io.FunSuiteCycleTests

trait GrpcFunSuiteCycleTests extends FunSuiteCycleTests {

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = GRPCConfiguration.GRPC()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
  }
}

class GrpcParserTest extends GrpcFunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/grpc/"

  multiGoldenTest("Can generate simple gRPC specs", "simple.%s") { config =>
    cycle(
      "simple.proto",
      config.golden,
      GrpcProtoHint,
      AmfJsonHint,
      renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint),
      eh = Some(UnhandledErrorHandler)
    )
  }
}

class StandardGoogleProtoParserTest extends GrpcFunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/grpc/google/"

  Seq(
    "any.proto",
    "api.proto",
    "duration.proto",
    "empty.proto",
    "field_mask.proto",
    "source_context.proto",
    "struct.proto",
    "timestamp.proto",
    "type.proto",
    "wrappers.proto"
  ).foreach { protoFile =>
    multiGoldenTest(s"Can generate standard google proto file ${protoFile}", s"${protoFile}.%s") { config =>
      cycle(
        protoFile,
        config.golden,
        GrpcProtoHint,
        AmfJsonHint,
        renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint),
        eh = Some(UnhandledErrorHandler)
      )
    }
  }
}
