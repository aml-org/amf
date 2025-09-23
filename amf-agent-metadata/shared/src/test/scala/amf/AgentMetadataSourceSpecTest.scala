package amf

import amf.core.client.common.transform.PipelineId
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.{Mimes, Spec}
import amf.agentmetadata.client.scala.AgentMetadataConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class AgentMetadataSourceSpecTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath      = "file://amf-agent-metadata/shared/src/test/resources/instances/"
  private val renderOptions = RenderOptions().withPrettyPrint.withCompactUris.withEntityEmission
  private val client        = AgentMetadataConfiguration.AgentMetadata().withRenderOptions(renderOptions).baseUnitClient()

//  test("Parsed JSON-LD from AgentMetadata should have AgentMetadata source spec") {
//    for {
//      result <- client.parse(basePath + "valid/instance_1.json")
//      jsonld = client.render(result.baseUnit, Mimes.`application/ld+json`)
//      jsonLdUnit <- client.parseContent(jsonld, "application/ld+json")
//    } yield {
//      result.conforms shouldBe true
//      jsonLdUnit.conforms shouldBe true
//      jsonLdUnit.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
//      jsonLdUnit.sourceSpec shouldBe Spec.AGENT_METADATA
//    }
//  }

  test("Transformation (empty) should conforms") {
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
