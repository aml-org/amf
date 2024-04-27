package amf.cycle

import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, AvroHint, GrpcProtoHint}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FunSuiteCycleTests
import org.scalatest.Assertion

import scala.concurrent.Future

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

  def cycle(source: String, target: String): Future[Assertion] = cycle(source, target, AvroHint, AmfJsonHint)

  test("Can parse declaration types") {
    cycle("declared-types.json", "declared-types.jsonld")
  }

  test("Can parse reference at types") {
    cycle("ref-types.json", "ref-types.jsonld")
  }

  test("Can parse union at field") {
    cycle("union-field.json", "union-field.jsonld")
  }

  test("Can parse inner type at field") {
    cycle("inner-type-field.json", "inner-type-field.jsonld")
  }

  test("Can parse message") {
    cycle("message.json", "message.jsonld")
  }

}
