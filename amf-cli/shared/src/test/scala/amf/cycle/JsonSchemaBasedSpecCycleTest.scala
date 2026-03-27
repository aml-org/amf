package amf.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.core.io.FileAssertionTest
import amf.mcp.client.scala.MCPConfiguration
import amf.shapes.client.scala.ShapesConfiguration
import org.scalatest.Assertion

import scala.concurrent.Future

class JsonSchemaBasedSpecCycleTest extends FileAssertionTest {

  // This suite is mean to test the lexical in general for JsonSchemaBasedSpecs. I choose to do it in MCP because is the easier one
  private val basePath          = "amf-mcp/shared/src/test/resources/instances/"
  private val renderOptions     = RenderOptions().withEntityEmission
  private val config            = MCPConfiguration.MCP()
  private val configWithOptions = MCPConfiguration.MCP().withRenderOptions(renderOptions)

  test("JSON-LD emission from MCP without RenderOption should not emit entities (JSON -> JSON-LD)") {
    cycle("valid/asset.json", "cycle/instance-without-entities.jsonld", config)
  }

  test("JSON-LD emission from MCP with RenderOption should emit entities (JSON -> JSON-LD)") {
    cycle("valid/asset.json", "cycle/instance-with-entities.jsonld", configWithOptions)
  }

  test("JSON emission from JSON-LD instance should emit correctly  (JSON -> JSON-LD -> JSON)") {
    cycle(
      "cycle/instance-with-entities.jsonld",
      "cycle/instance-cycled.jsonld.json",
      configWithOptions,
      Mimes.`application/json`
    )
  }

  test("JSON-LD emission from JSON-LD instance should emit correctly  (JSON -> JSON-LD -> JSON-LD)") {
    cycle(
      "cycle/instance-with-entities.jsonld",
      "cycle/instance-cycled.jsonld.jsonld",
      configWithOptions
    )
  }

  def cycle(
      source: String,
      golden: String,
      config: ShapesConfiguration,
      mediaType: String = Mimes.`application/ld+json`
  ): Future[Assertion] = {
    for {
      parsed <- config.baseUnitClient().parse("file://" + basePath + source)
      actualString = config.baseUnitClient().render(parsed.baseUnit, mediaType)
      actualFile <- writeTemporaryFile(golden)(actualString)
      assertion  <- assertDifferences(actualFile, basePath + golden)
    } yield {
      assertion
    }
  }
}
