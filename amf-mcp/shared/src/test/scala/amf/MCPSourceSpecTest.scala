package amf

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import amf.mcp.client.scala.MCPConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class MCPSourceSpecTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath = "file://amf-mcp/shared/src/test/resources/instances/"
  private val client   = MCPConfiguration.MCP().withRenderOptions(RenderOptions().withEntityEmission).baseUnitClient()

  test("Parsed JSON-LD from MCP should have MCP source spec") {
    for {
      result <- client.parse(basePath + "valid/instance_1.json")
      jsonld = client.render(result.baseUnit, `application/ld+json`)
      cycledResult <- client.parseContent(jsonld)
    } yield {
      result.conforms shouldBe true
      cycledResult.conforms shouldBe true
      result.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      cycledResult.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      cycledResult.sourceSpec shouldBe Spec.MCP
      val encodes       = result.baseUnit.asInstanceOf[JsonLDInstanceDocument].encodes
      val cycledEncodes = cycledResult.baseUnit.asInstanceOf[JsonLDInstanceDocument].encodes
      encodes.headOption shouldBe defined
      cycledEncodes.headOption shouldBe defined
      encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      cycledEncodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val rootPropertiesSize       = encodes.head.asInstanceOf[JsonLDObject].fields.fields().size
      val cycledRootPropertiesSize = cycledEncodes.head.asInstanceOf[JsonLDObject].fields.fields().size
      rootPropertiesSize shouldBe cycledRootPropertiesSize
    }
  }

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
