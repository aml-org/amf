package amf.client.resolve

import amf.client.convert.CoreClientConverters
import amf.client.convert.CoreClientConverters._
import amf.client.model.document.BaseUnit
import amf.core.model.document.{BaseUnit => InternalBaseUnit}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeResolver

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class Resolver(vendor: String) {
  def resolve(unit: BaseUnit): BaseUnit = {
    convert(RuntimeResolver.resolve(vendor, unit._internal, ResolutionPipeline.DEFAULT_PIPELINE))
  }

  def resolve(unit: BaseUnit, pipelineId: String) = convert(RuntimeResolver.resolve(vendor, unit._internal, pipelineId))

  protected def convert(unit: InternalBaseUnit) = {
    CoreClientConverters.asClient(unit)
  }
}
