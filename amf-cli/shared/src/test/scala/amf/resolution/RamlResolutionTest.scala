package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.model.document.BaseUnit
import amf.emit.AMFRenderer

import scala.concurrent.Future

abstract class RamlResolutionTest extends ResolutionTest {

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

}
