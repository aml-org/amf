package amf.shapes.client.jsonldschema.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.Assertion

import scala.concurrent.Future

class JsonSchemaCycleTest extends FileAssertionTest {

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/cycle"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def run(testFile: String): Future[Assertion] = {
    for {
      jsonDocument <- client.parseJsonLDSchema(s"file://$basePath/$testFile").map(_.jsonDocument)
      tmp <- writeTemporaryFile(s"$basePath/$testFile.json")(client.render(jsonDocument, Mimes.`application/json`))
      diff <- assertDifferences(tmp, s"$basePath/$testFile.json")
    } yield diff
  }

  test("Context with @container as string") {
    run("context-container.json")
  }

  test("Context with @container as seq") {
    run("context-container-seq.json")
  }

}
