package amf.resolution

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.io.{FunSuiteCycleTests, MultiJsonldAsyncFunSuite}
import amf.plugins.document.webapi.resolution.pipelines.{AmfEditingPipeline, AmfResolutionPipeline}
import amf.plugins.document.webapi.{Async20Plugin, Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

abstract class ResolutionTest extends FunSuiteCycleTests {

  val defaultPipelineToUse: String = ResolutionPipeline.DEFAULT_PIPELINE
  val defaultVendor: Option[Vendor] = None

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val pipeline = config.pipeline.getOrElse(defaultPipelineToUse)
    val vendor   = config.transformWith.orElse(defaultVendor).getOrElse(config.target)
    val res = vendor match {
      case Raml08                => Raml08Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
      case AsyncApi | AsyncApi20 => Async20Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
      case Raml | Raml10         => Raml10Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
      case Oas30                 => Oas30Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
      case Oas | Oas20           => Oas20Plugin.resolve(unit, UnhandledErrorHandler, pipeline)
      case Amf                   => UnhandledAmfPipeline(pipeline).resolve(unit)
      case target                => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }

  object UnhandledAmfPipeline {
    def apply(pipeline: String) = pipeline match {
      case ResolutionPipeline.EDITING_PIPELINE => AmfEditingPipeline.unhandled
      case ResolutionPipeline.DEFAULT_PIPELINE => AmfResolutionPipeline.unhandled
      case _                                   => throw new Exception(s"Cannot amf pipeline: $pipeline")
    }
  }
}
