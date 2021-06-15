package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.emit.AMFRenderer

import scala.concurrent.Future

abstract class RamlResolutionTest extends ResolutionTest {

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

}
