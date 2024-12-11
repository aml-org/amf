package amf.shapes.client.jsonldschema.spec

import amf.core.client.scala.config.RenderOptions
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}

class GCLInstanceSpecParsingTest extends FileAssertionTest {

  private lazy val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema/gcl/"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  test(s"Test case instace1") {
    for {
      jsonDocument <- client.parseJsonLDSchema("file://" + basePath + "schemav1.json").map(_.jsonDocument)
      instance <- client.parseJsonLDInstance("file://" + basePath + "instance1.yaml", jsonDocument).map(_.instance)
      tmp <- writeTemporaryFile(basePath + "instance1.jsonld")(
        client.render(instance, "application/schemald+json")
      )
      r <- assertDifferences(tmp, basePath + "instance1.jsonld")
    } yield r
  }

}
