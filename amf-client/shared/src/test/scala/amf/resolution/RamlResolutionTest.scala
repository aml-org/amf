package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.model.document.BaseUnit
import amf.emit.AMFRenderer

import scala.concurrent.Future

abstract class RamlResolutionTest extends ResolutionTest {

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    new AMFRenderer(unit, config.target, defaultRenderOptions, config.syntax).renderToString
  }
}
