package amf.shapes.client.jsonldschema

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.funsuite.AsyncFunSuite

class JsonLDSchemaSpecParsingTest extends AsyncFunSuite with FileAssertionTest {
  val basePath: String      = "amf-shapes/shared/src/test/resources/jsonld-schema/"
  val schemasPath: String   = "file://" + basePath + "schemas/"
  val instancesPath: String = "file://" + basePath + "instances/"
  val resultsPath: String   = "file://" + basePath + "instances/results/"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  platform.fs.syncFile(basePath + "schemas").list.foreach { schema =>
    if (platform.fs.syncFile(basePath + "instances/" + schema).exists) {
      test(s"Test case $schema") {
        run(schema)
      }
    }

  }

  def run(schema: String) = {
    for {
      jsonDocument <- client.parseJsonLDSchema(schemasPath + schema).map(_.jsonDocument)
      instance     <- client.parseJsonLDInstance(instancesPath + schema, jsonDocument).map(_.instance)
      tmp <- writeTemporaryFile(resultsPath + schema + ".jsonld")(client.render(instance, Mimes.`application/ld+json`))
      r   <- assertDifferences(tmp, resultsPath + schema + ".jsonld")
    } yield r

  }

}
