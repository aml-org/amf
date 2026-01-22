package amf

import amf.core.common.FileAssertionTest
import amf.core.internal.remote.Mimes
import amf.agentgraph.client.scala.AgentGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.shapes.client.scala.ShapesConfiguration
import org.scalatest.Assertion

import scala.concurrent.Future

class AgentGraphCycleTest extends FileAssertionTest {
  private val basePath: String                 = "amf-agent-graph/shared/src/test/resources/instances/"
  private val renderOptions                    = RenderOptions().withPrettyPrint
  private val agentConfig: ShapesConfiguration = AgentGraphConfiguration.AgentGraph().withRenderOptions(renderOptions)


  test("Render AgentGraph YAML instance to JSON-LD") {
    cycle("valid/instance_1.yaml", "valid/instance_1.jsonld")
  }

  def cycle(source: String, golden: String): Future[Assertion] = {
    for {
      parsed <- agentConfig.baseUnitClient().parse("file://" + basePath + source)
      actualString = agentConfig.baseUnitClient().render(parsed.baseUnit, Mimes.`application/ld+json`)
      actualFile <- writeTemporaryFile(golden)(actualString)
      assertion  <- assertDifferences(actualFile, basePath + golden)
    } yield {
      assertion
    }
  }
}
