package amf

import amf.core.client.common.transform.PipelineId
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.{Mimes, Spec}
import amf.agentdomain.client.scala.AgentDomainConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class AgentDomainSourceSpecTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath = "file://amf-agent-domain/shared/src/test/resources/instances/"

  test("Parsed JSON-LD from AgentDomain should have AgentDomain source spec") {
    val client = AgentDomainConfiguration.AgentDomain().baseUnitClient()
    for {
      result     <- client.parse(basePath + "valid/instance_1.json")
      jsonld     = client.render(result.baseUnit, Mimes.`application/ld+json`)
      jsonLdUnit <- client.parseContent(jsonld, "application/ld+json")
    } yield {
      result.conforms shouldBe true
      jsonLdUnit.conforms shouldBe true
      jsonLdUnit.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      jsonLdUnit.sourceSpec shouldBe Spec.AGENT_DOMAIN
    }
  }

  test("Transformation (empty) should conforms") {
    val client = AgentDomainConfiguration.AgentDomain().baseUnitClient()
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
