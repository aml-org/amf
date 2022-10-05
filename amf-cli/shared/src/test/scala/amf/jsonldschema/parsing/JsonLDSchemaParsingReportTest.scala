package amf.jsonldschema.parsing

import amf.core.client.scala.config.RenderOptions
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.ExecutionContext

class JsonLDSchemaParsingReportTest extends AsyncFunSuite with FileAssertionTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String    = "amf-shapes/shared/src/test/resources/jsonld-schema/parsing"
  private val reportsPath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/reports"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  test("(Invalid) Scalar at root") {
    val testName = "root-scalar"
    for {
      jsonDocument   <- client.parseJsonLDSchema(s"file://$basePath/$testName/schema.json").map(_.jsonDocument)
      instanceResult <- client.parseJsonLDInstance(s"file://$basePath/$testName/instance.json", jsonDocument)
      tmp            <- writeTemporaryFile(s"$reportsPath/$testName.report")(instanceResult.toString)
      diff           <- assertDifferences(tmp, s"$reportsPath/$testName.report")
    } yield diff
  }

}
