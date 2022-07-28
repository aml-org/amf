package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}
import amf.graphql.client.scala.GraphQLConfiguration

class GraphQLRecursionCycleTest extends GraphQLFunSuiteCycleTests {
  // valid
  fs.syncFile(s"$basePath/valid").list.foreach { file =>
    if (file.endsWith(".graphql")) {
      test(s"GraphQL Recursion > valid > $file") {
        cycle(file, file.replace(".graphql", ".jsonld"), GraphQLHint, AmfJsonHint, directory = s"$basePath/valid/")
      }
    }
  }

  // invalid
  fs.syncFile(s"$basePath/invalid").list.foreach { file =>
    if (file.endsWith(".graphql")) {
      test(s"GraphQL Recursion > invalid > $file") {
        cycle(file, file.replace(".graphql", ".jsonld"), GraphQLHint, AmfJsonHint, directory = s"$basePath/invalid/")
      }
    }
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    GraphQLConfiguration.GraphQL().baseUnitClient().transform(unit, PipelineId.Cache).baseUnit
  }

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/graphql/recursion/"
}
