package amf.resolution

import amf.client.environment.AMFConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests

abstract class ResolutionTest extends FunSuiteCycleTests with ResolutionCapabilities {

  val defaultPipelineToUse: String  = TransformationPipeline.DEFAULT_PIPELINE
  val defaultVendor: Option[Vendor] = None

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    val pipeline = config.pipeline.getOrElse(defaultPipelineToUse)
    val vendor   = config.transformWith.orElse(defaultVendor).getOrElse(config.target)
    transform(unit, pipeline, vendor, amfConfig)
  }
}
