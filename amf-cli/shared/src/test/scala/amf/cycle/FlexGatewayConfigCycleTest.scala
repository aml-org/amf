package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.apiinstance.client.scala.APIInstanceConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, EnvoyHint}
import amf.io.FunSuiteCycleTests

trait FlexGatewayConfigFunSuiteCycleTests extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = APIInstanceConfiguration.APIInstance()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
  }
}


class FlexGatewayConfigCycleTest extends FlexGatewayConfigFunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/flex/"

  test("HERE_HERE Can parse a simple Flex config") {
    cycle("simple/config.yaml", "simple/config.jsonld", EnvoyHint, AmfJsonHint)
  }
}
