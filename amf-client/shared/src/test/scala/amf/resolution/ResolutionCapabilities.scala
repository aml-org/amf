package amf.resolution

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, AsyncApi, AsyncApi20, Oas, Oas20, Oas30, Raml, Raml08, Raml10, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.{AmfEditingPipeline, AmfResolutionPipeline}
import amf.plugins.document.webapi.{Async20Plugin, Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

trait ResolutionCapabilities {
  protected def transform(unit: BaseUnit, pipeline: String, vendor: Vendor): BaseUnit = vendor match {
    case Raml08                => Raml08Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
    case AsyncApi | AsyncApi20 => Async20Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
    case Raml | Raml10         => Raml10Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
    case Oas30                 => Oas30Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
    case Oas | Oas20           => Oas20Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
    case Amf                   => UnhandledAmfPipeline(pipeline).resolve(unit)
    case target                => throw new Exception(s"Cannot resolve $target")
    //    case _ => unit
  }

  object UnhandledAmfPipeline {
    def apply(pipeline: String) = pipeline match {
      case ResolutionPipeline.EDITING_PIPELINE => AmfEditingPipeline.unhandled
      case ResolutionPipeline.DEFAULT_PIPELINE => AmfResolutionPipeline.unhandled
      case _                                   => throw new Exception(s"Cannot amf pipeline: $pipeline")
    }
  }
}
