package amf.shapes.client.jsonldschema.parsing

import amf.core.client.scala.config.RenderOptions
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

// Test suite for invalid parsing results
class JsonLDSchemaParsingReportTest extends AsyncFunSuite with FileAssertionTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String    = "amf-shapes/shared/src/test/resources/jsonld-schema/parsing"
  private val reportsPath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/reports"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def run(testName: String): Future[Assertion] = {
    for {
      jsonDocument   <- client.parseJsonLDSchema(s"file://$basePath/$testName/schema.json").map(_.jsonDocument)
      instanceResult <- client.parseJsonLDInstance(s"file://$basePath/$testName/instance.json", jsonDocument)
      tmp            <- writeTemporaryFile(s"$reportsPath/$testName.report")(instanceResult.toString)
      diff           <- assertDifferences(tmp, s"$reportsPath/$testName.report")
    } yield diff
  }

  test("Scalar at root") {
    run("root-scalar")
  }

  test("Non array element with @list container") {
    run("invalid-container-list-instance")
  }

}
