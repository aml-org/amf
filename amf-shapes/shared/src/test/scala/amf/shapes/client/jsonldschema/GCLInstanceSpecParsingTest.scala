package amf.shapes.client.jsonldschema

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.funsuite.AsyncFunSuite

class GCLInstanceSpecParsingTest extends AsyncFunSuite with FileAssertionTest {
  private lazy val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/gcl/"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  test(s"Test case instace1") {
    for {
      jsonDocument <- client.parseJsonLDSchema("file://" + basePath + "schemav1.json").map(_.jsonDocument)
      instance     <- client.parseJsonLDInstance("file://" + basePath + "instance1.yaml", jsonDocument).map(_.instance)
      tmp <- writeTemporaryFile(basePath + "instance1.jsonld")(
        client.render(instance, Mimes.`application/ld+json`)
      )
      r <- assertDifferences(tmp, basePath + "instance1.jsonld")
    } yield r
  }

}
