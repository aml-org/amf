package amf.resolution

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.io.{FunSuiteCycleTests, MultiJsonldAsyncFunSuite}
import amf.plugins.document.webapi.resolution.pipelines.{AmfEditingPipeline, AmfResolutionPipeline}
import amf.plugins.document.webapi.{Async20Plugin, Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

abstract class ResolutionTest extends FunSuiteCycleTests with ResolutionCapabilities {

  val defaultPipelineToUse: String  = ResolutionPipeline.DEFAULT_PIPELINE
  val defaultVendor: Option[Vendor] = None

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val pipeline = config.pipeline.getOrElse(defaultPipelineToUse)
    val vendor   = config.transformWith.orElse(defaultVendor).getOrElse(config.target)
    transform(unit, pipeline, vendor)
  }
}
