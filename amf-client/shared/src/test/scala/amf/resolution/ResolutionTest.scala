package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.emit.AMFRenderer
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

import scala.concurrent.Future

abstract class ResolutionTest extends FunSuiteCycleTests {

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08        => Raml08Plugin.resolve(unit, UnhandledErrorHandler)
      case Raml | Raml10 => Raml10Plugin.resolve(unit, UnhandledErrorHandler)
      case Oas30         => Oas30Plugin.resolve(unit, UnhandledErrorHandler)
      case Oas | Oas20   => Oas20Plugin.resolve(unit, UnhandledErrorHandler)
      case Amf           => AmfResolutionPipeline.unhandled.resolve(unit)
      case target        => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] =
    new AMFRenderer(unit, Amf, RenderOptions().withSourceMaps.withPrettyPrint, config.syntax).renderToString
}
