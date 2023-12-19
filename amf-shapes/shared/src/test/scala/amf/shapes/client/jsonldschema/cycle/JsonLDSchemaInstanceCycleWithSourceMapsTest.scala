package amf.shapes.client.jsonldschema.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class JsonLDSchemaInstanceCycleWithSourceMapsTest extends AsyncFunSuite with FileAssertionTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/cycle-jsonld-sourcemaps"


  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint.withSourceMaps).baseUnitClient()

  private def run(testFolder: String): Future[Assertion] = {
    val testPath = s"$basePath/$testFolder"
    for {
      schema     <- client.parseJsonLDSchema(s"file://$testPath/schema.json").map(_.jsonDocument)
      instance   <- client.parseJsonLDInstance(s"file://$testPath/instance.json", schema)
      instanceLD <- Future.successful(client.render(instance.baseUnit, Mimes.`application/ld+json`))
      tmp        <- writeTemporaryFile(s"/instance.jsonld")(instanceLD)
      diff       <- assertDifferences(tmp, s"$testPath/instance.jsonld")
    } yield diff
  }

  test("Test properties") {
    run("properties")
  }

}
