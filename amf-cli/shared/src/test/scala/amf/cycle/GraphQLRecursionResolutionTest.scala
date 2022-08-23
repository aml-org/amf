package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.GraphQLProfile
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.{AmfJsonHint, GraphQLHint}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FileAssertionTest
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class GraphQLRecursionResolutionTest extends GraphQLFunSuiteCycleTests {
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

class GraphQLRecursionValidationTest extends AsyncFunSuite with FileAssertionTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/graphql/recursion/"

  // valid
  fs.syncFile(s"$basePath/valid").list.foreach { file =>
    if (file.endsWith(".graphql")) {
      test(s"GraphQL Recursion > valid > $file") {
        validate(s"$basePath/valid/$file").map { report =>
          assert(report.conforms)
        }
      }
    }
  }

  // invalid
  fs.syncFile(s"$basePath/invalid").list.foreach { file =>
    if (file.endsWith(".graphql")) {
      test(s"GraphQL Recursion > invalid > $file") {
        val fullPath = s"$basePath/invalid/$file"
        val golden   = fullPath.replace(".graphql", ".report")
        for {
          report    <- validate(s"$basePath/invalid/$file")
          tmpFile   <- writeTemporaryFile(golden)(report.toString)
          assertion <- assertDifferences(tmpFile, golden)
        } yield {
          assertion
        }
      }
    }
  }

  private def validate(file: String): Future[AMFValidationReport] = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    for {
      pResult <- client.parse(s"file://$file")
    } yield {
      val results = client.transform(pResult.baseUnit, PipelineId.Cache).results
      AMFValidationReport(pResult.baseUnit.id, GraphQLProfile, results)
    }
  }
}
