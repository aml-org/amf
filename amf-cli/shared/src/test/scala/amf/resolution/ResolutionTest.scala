package amf.resolution

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests
import amf.core.client.common.transform._

abstract class ResolutionTest extends FunSuiteCycleTests with ResolutionCapabilities {

  val defaultPipeline: String     = PipelineId.Default
  val defaultVendor: Option[Spec] = None

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    val pipeline = config.pipeline.getOrElse(defaultPipeline)
    val vendor   = config.transformWith.orElse(defaultVendor).getOrElse(config.renderTarget.spec)
    transform(unit, pipeline, vendor, amfConfig)
  }
}
