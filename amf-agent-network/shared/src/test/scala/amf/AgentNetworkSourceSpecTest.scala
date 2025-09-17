package amf

import amf.agentnetwork.client.scala.AgentNetworkConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.{Mimes, Spec}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import org.scalatest.matchers.should.Matchers

class AgentNetworkSourceSpecTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath = "file://amf-agent-network/shared/src/test/resources/instances/"
  private val renderOptions = RenderOptions().withPrettyPrint.withCompactUris.withEntityEmission
  private val client        = AgentNetworkConfiguration.AgentNetwork().withRenderOptions(renderOptions).baseUnitClient()

  test("Parsed JSON-LD from AgentNetwork should have AgentNetwork source spec") {
    for {
      result     <- client.parse(basePath + "valid/instance_1.json")
      jsonld     = client.render(result.baseUnit, Mimes.`application/ld+json`)
      jsonLdUnit <- client.parseContent(jsonld, "application/ld+json")
    } yield {
      result.conforms shouldBe true
      jsonLdUnit.conforms shouldBe true
      jsonLdUnit.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      jsonLdUnit.sourceSpec shouldBe Spec.AGENT_NETWORK
    }
  }

  test("Transformation (empty) should conform") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.json")
      transformationDefault = client.transform(parseResult.baseUnit.cloneUnit(), PipelineId.Default)
      transformationEditing = client.transform(parseResult.baseUnit.cloneUnit(), PipelineId.Editing)
      transformationCache   = client.transform(parseResult.baseUnit.cloneUnit(), PipelineId.Cache)
    } yield {
      parseResult.conforms shouldBe true
      transformationDefault.conforms shouldBe true
      transformationEditing.conforms shouldBe true
      transformationCache.conforms shouldBe true
      transformationDefault.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      transformationEditing.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      transformationCache.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
    }
  }
}
