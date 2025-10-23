package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, GrpcProtoHint, Spec}
import amf.grpc.client.scala.GRPCConfiguration
import amf.resolution.ResolutionTest

class GrpcCycleResolutionTest extends ResolutionTest {

  private val config: AMFConfiguration =
    GRPCConfiguration.GRPC().withRenderOptions(renderOptions()).withErrorHandlerProvider(() => IgnoringErrorHandler)

  override def basePath: String               = "amf-cli/shared/src/test/resources/upanddown/cycle/grpc/"
  override def renderOptions(): RenderOptions = RenderOptions().withPrettyPrint // I want to render flattened

  override val defaultPipeline: String     = PipelineId.Editing
  override val defaultVendor: Option[Spec] = Some(Spec.GRPC)

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = config

  test("GRPC complete example to JSON-LD") {
    cycle("all/main.proto", "all/dumped.resolved.jsonld", GrpcProtoHint, AmfJsonHint, amfConfig = Some(config))
  }
}
