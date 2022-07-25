package amf.graphql

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint, Spec}

class GraphQLTCKResolutionTest extends GraphQLFunSuiteCycleTests {
  override def basePath: String = s"amf-graphql-test-sets/shared/src/test/resources/graphql/tck/apis/valid/"

  /**
    * We should not resolve inheritance in GraphQL, only validate it
    * We should detect recursions
    *
    * Calling ShapeNormalization will resolve inheritance and detect recursions. We only want the later. Skipping recursive tests
    */
  private val ignored = Seq(
    "is-input-type-fields.graphql",
    "recursion.api.graphql"
  )

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".graphql") && !api.endsWith(".dumped.graphql") && !ignored.contains(api)) {
      test(s"GraphQL TCK > Apis > Valid > $api: resolved dumped JSON matches golden") {
        cycle(api, api.replace(".graphql", ".resolved.jsonld"), GraphQLHint, AmfJsonHint, transformWith = Some(Spec.GRAPHQL))
      }
    }
  }


  override def renderOptions(): RenderOptions = RenderOptions().withPrettyPrint.withoutFlattenedJsonLd

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    amfConfig.baseUnitClient().transform(unit, PipelineId.Cache).baseUnit
  }
}
