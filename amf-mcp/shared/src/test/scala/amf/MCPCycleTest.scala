package amf

import amf.core.client.scala.config.RenderOptions
import amf.core.common.FileAssertionTest
import amf.core.internal.remote.Mimes
import amf.mcp.client.scala.MCPConfiguration
import amf.shapes.client.scala.ShapesConfiguration
import org.scalatest.Assertion

import scala.concurrent.Future

class MCPCycleTest extends FileAssertionTest {
  private val basePath: String               = "amf-mcp/shared/src/test/resources/instances/"
  private val renderOptions                  = RenderOptions().withPrettyPrint
  private val mcpConfig: ShapesConfiguration = MCPConfiguration.MCP().withRenderOptions(renderOptions)

  test("Render MCP JSON instance to JSON-LD") {
    cycle("valid/instance_1.json", "valid/instance_1.json.jsonld")
  }

  test("Render MCP YAML instance to JSON-LD") {
    cycle("valid/instance_1.yaml", "valid/instance_1.yaml.jsonld")
  }

  test("Render MCP JSON instance 2 to JSON-LD") {
    cycle("valid/instance_2.json", "valid/instance_2.json.jsonld")
  }

  test("Render MCP YAML instance 2 to JSON-LD") {
    cycle("valid/instance_2.yaml", "valid/instance_2.yaml.jsonld")
  }

  def cycle(source: String, golden: String): Future[Assertion] = {
    for {
      parsed <- mcpConfig.baseUnitClient().parse("file://" + basePath + source)
      actualString = mcpConfig.baseUnitClient().render(parsed.baseUnit, Mimes.`application/ld+json`)
      actualFile <- writeTemporaryFile(golden)(actualString)
      assertion  <- assertDifferences(actualFile, basePath + golden)
    } yield {
      assertion
    }
  }
}
