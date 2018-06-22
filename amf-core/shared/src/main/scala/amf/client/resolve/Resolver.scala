package amf.client.resolve

import amf.client.convert.CoreClientConverters._
import amf.client.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeResolver

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("Resolver")
@JSExportAll
class Resolver(vendor: String) {

  def resolve(unit: BaseUnit): BaseUnit = resolve(unit, ResolutionPipeline.DEFAULT_PIPELINE)

  def resolve(unit: BaseUnit, pipeline: String): BaseUnit = RuntimeResolver.resolve(vendor, unit, pipeline)
}
