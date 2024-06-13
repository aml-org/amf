package amf.avro

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
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(IgnoringErrorHandler))
  }

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/avro/"

  def cycle(source: String, target: String): Future[Assertion] = cycle(source, target, AvroHint, AmfJsonHint)
}
