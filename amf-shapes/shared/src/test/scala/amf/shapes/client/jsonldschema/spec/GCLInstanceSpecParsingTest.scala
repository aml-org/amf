package amf.shapes.client.jsonldschema.spec

import amf.core.client.scala.config.RenderOptions
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.ExecutionContext

class GCLInstanceSpecParsingTest extends AsyncFunSuite with FileAssertionTest {
  private lazy val basePath: String                        = "amf-shapes/shared/src/test/resources/jsonld-schema/gcl/"
  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  test(s"Test case instace1") {
    for {
      jsonDocument <- client.parseJsonLDSchema("file://" + basePath + "schemav1.json").map(_.jsonDocument)
      instance     <- client.parseJsonLDInstance("file://" + basePath + "instance1.yaml", jsonDocument).map(_.instance)
      tmp <- writeTemporaryFile(basePath + "instance1.jsonld")(
        client.render(instance, "application/schemald+json")
      )
      r <- assertDifferences(tmp, basePath + "instance1.jsonld")
    } yield r
  }

}
