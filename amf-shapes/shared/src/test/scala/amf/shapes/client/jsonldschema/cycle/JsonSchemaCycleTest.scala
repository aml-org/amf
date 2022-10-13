package amf.shapes.client.jsonldschema.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

// Test suite for invalid parsing results
class JsonSchemaCycleTest extends AsyncFunSuite with FileAssertionTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/cycle"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def run(testFile: String): Future[Assertion] = {
    for {
      jsonDocument <- client.parseJsonLDSchema(s"file://$basePath/$testFile").map(_.jsonDocument)
      tmp  <- writeTemporaryFile(s"$basePath/$testFile.json")(client.render(jsonDocument, Mimes.`application/json`))
      diff <- assertDifferences(tmp, s"$basePath/$testFile.json")
    } yield diff
  }

  test("Context with @container") {
    run("context-container.json")
  }

}
