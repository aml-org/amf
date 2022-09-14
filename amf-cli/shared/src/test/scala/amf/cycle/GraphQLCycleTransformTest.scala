package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint, Spec}

class GraphQLCycleTransformTest extends GraphQLFunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/graphql/"



  test("Can parse and resolve API with simple recursive inheritance") {
    val base = "recursive-inheritance"
    val ro   = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint.withSourceMaps
    cycle(s"$base/simple.graphql", s"$base/simple.resolved.jsonld", GraphQLHint, AmfJsonHint, renderOptions = Some(ro), transformWith = Some(Spec.GRAPHQL))
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    amfConfig.baseUnitClient().transform(unit, PipelineId.Cache).baseUnit
  }
}
