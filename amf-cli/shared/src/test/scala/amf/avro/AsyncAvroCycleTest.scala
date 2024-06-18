package amf.avro

import amf.apicontract.client.scala.{AMFConfiguration, AsyncAPIConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint}
import amf.io.FunSuiteCycleTests
import org.scalatest.Assertion

import scala.concurrent.Future

class AsyncAvroCycleTest extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    AsyncAPIConfiguration
      .Async20()
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(IgnoringErrorHandler))
  }

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/avro/"

  def cycle(source: String, target: String): Future[Assertion] = cycle(source, target, Async20YamlHint, AmfJsonHint)
}
