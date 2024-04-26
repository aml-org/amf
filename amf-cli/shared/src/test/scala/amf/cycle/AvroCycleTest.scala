package amf.cycle

import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, AvroHint, GrpcProtoHint}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FunSuiteCycleTests

class AvroCycleTest extends FunSuiteCycleTests {

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = AvroConfiguration.Avro()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e)
    )
  }

  override def renderOptions(): RenderOptions = super.renderOptions().withPrettyPrint

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/avro/"

  test("Can parse declaration types") {
    cycle("declared-types.json", "declared-types.jsonld", AvroHint, AmfJsonHint)
  }

  test("Can parse reference at types") {
    cycle("ref-types.json", "ref-types.jsonld", AvroHint, AmfJsonHint)
  }

}
