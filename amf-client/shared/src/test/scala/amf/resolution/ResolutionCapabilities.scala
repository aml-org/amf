package amf.resolution

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, AsyncApi, AsyncApi20, Oas, Oas20, Oas30, Raml, Raml08, Raml10, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeResolver
import amf.plugins.document.webapi.resolution.pipelines.{AmfEditingPipeline, AmfResolutionPipeline}
import amf.plugins.document.webapi.{Async20Plugin, Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

trait ResolutionCapabilities {
  protected def transform(unit: BaseUnit, pipeline: String, vendor: Vendor): BaseUnit = vendor match {
    case AsyncApi | AsyncApi20 | Raml | Raml08 | Raml10 | Oas | Oas20 | Oas30 =>
      RuntimeResolver.resolve(vendor.name, unit, pipeline, UnhandledErrorHandler)
    case Amf    => UnhandledAmfPipeline(pipeline).transform(unit, vendor.name, UnhandledErrorHandler)
    case target => throw new Exception(s"Cannot resolve $target")
    //    case _ => unit
  }

  object UnhandledAmfPipeline {
    def apply(pipeline: String) = pipeline match {
      case ResolutionPipeline.EDITING_PIPELINE => new AmfEditingPipeline()
      case ResolutionPipeline.DEFAULT_PIPELINE => new AmfResolutionPipeline()
      case _                                   => throw new Exception(s"Cannot amf pipeline: $pipeline")
    }
  }
}
