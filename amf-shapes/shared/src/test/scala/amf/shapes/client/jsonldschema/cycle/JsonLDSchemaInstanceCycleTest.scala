package amf.shapes.client.jsonldschema.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.Assertion

import scala.concurrent.Future

class JsonLDSchemaInstanceCycleTest extends FileAssertionTest {

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/cycle-jsonld"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  private def run(testFolder: String): Future[Assertion] = {
    val testPath = s"$basePath/$testFolder"
    for {
      schema <- client.parseJsonLDSchema(s"file://$testPath/schema.json").map(_.jsonDocument)
      instance <- client.parseJsonLDInstance(s"file://$testPath/instance.json", schema)
      instanceLD <- Future.successful(client.render(instance.baseUnit, Mimes.`application/ld+json`))
      tmp <- writeTemporaryFile(s"/instance.jsonld")(instanceLD)
      diff <- assertDifferences(tmp, s"$testPath/instance.jsonld")
    } yield diff
  }

  test("Test uri expansion with characteristics simple") {
    run("uri-expansion/characteristics-simple")
  }

  test("Test uri expansion with characteristics with nested alias") {
    run("uri-expansion/characteristics-nested")
  }

  test("Test uri expansion with property term simple") {
    run("uri-expansion/property-term-simple")
  }

  test("Test uri expansion with property term with nested alias") {
    run("uri-expansion/property-term-nested")
  }

  test("Test uri expansion with property term expanded @id") {
    run("uri-expansion/property-term-expanded")
  }

  test("Test uri expansion with class term simple") {
    run("uri-expansion/class-term-simple")
  }

  test("Test uri expansion with class term with nested alias") {
    run("uri-expansion/class-term-nested")
  }

  test("Test uri expansion with class term complex") {
    run("uri-expansion/class-term-complex")
  }
}
