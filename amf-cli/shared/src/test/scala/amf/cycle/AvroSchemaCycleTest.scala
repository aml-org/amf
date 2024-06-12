package amf.cycle
import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, AvroHint}
import amf.io.FunSuiteCycleTests
import org.scalatest.Assertion

import scala.concurrent.Future
class AvroSchemaCycleTest extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    AvroConfiguration
      .Avro()
      .withRenderOptions(renderOptions().withPrettyPrint)
      .withErrorHandlerProvider(() => IgnoringErrorHandler)
  }

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/avro/"

  def cycle(source: String, target: String): Future[Assertion] = cycle(source, target, AvroHint, AmfJsonHint)

  test("Can parse an array") {
    cycle("array.json", "array.jsonld")
  }

  test("Can parse an enum") {
    cycle("enum.json", "enum.jsonld")
  }

  test("Can parse a fixed shape") {
    cycle("fixed.json", "fixed.jsonld")
  }

  test("Can parse a map") {
    cycle("map.json", "map.jsonld")
  }

  test("Can parse a record with a recursive reference") {
    cycle("record.json", "record.jsonld")
  }
}
