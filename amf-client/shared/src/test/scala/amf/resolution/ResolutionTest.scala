package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.facades.AMFRenderer
import amf.io.BuildCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.document.webapi.{OAS20Plugin, OAS30Plugin, RAML08Plugin, RAML10Plugin}

import scala.concurrent.Future

abstract class ResolutionTest extends BuildCycleTests {

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08        => RAML08Plugin.resolve(unit)
      case Raml | Raml10 => RAML10Plugin.resolve(unit)
      case Oas3          => OAS30Plugin.resolve(unit)
      case Oas | Oas2    => OAS20Plugin.resolve(unit)
      case Amf           => new AmfResolutionPipeline(unit).resolve()
      case target        => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] =
    new AMFRenderer(unit, Amf, Amf.defaultSyntax, RenderOptions().withSourceMaps).renderToString
}
