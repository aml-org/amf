package amf.cycle

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{Grpc, GrpcProtoHint, Hint, PayloadJsonHint, Raml10YamlHint, Spec}
import amf.core.internal.remote.Syntax.{Json, Protobuf}
import amf.sfdc.client.scala.SFDCConfiguration
import amf.io.FunSuiteCycleTests
import amf.sfdc.plugins.parse.SfdcHint



class SfdcCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/sfdc/"

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = SFDCConfiguration.SFDC()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
  }

  test("Can cycle through a simple SFDC API") {
    val raml10Client: AMFBaseUnitClient = RAMLConfiguration.RAML10().baseUnitClient()

    SFDCConfiguration.SFDC().baseUnitClient().parse("file://amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.json") map
      { parseResult =>
        val transformResult = raml10Client.transform(parseResult.baseUnit, PipelineId.Compatibility)
        val renderResult = raml10Client.render(transformResult.baseUnit)
        println(renderResult)
      }

    cycle("sfdc.json", "sfdc.yaml", SfdcHint, Raml10YamlHint)
  }
}
