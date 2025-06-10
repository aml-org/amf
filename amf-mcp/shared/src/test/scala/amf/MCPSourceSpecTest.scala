package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.{Mimes, Spec}
import amf.mcp.client.scala.MCPConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class MCPSourceSpecTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath = "file://amf-mcp/shared/src/test/resources/instances/"

  test("Parsed JSON-LD from MCP should have MCP source spec") {
    val client = MCPConfiguration.MCP().baseUnitClient()
    for {
      result <- client.parse(basePath + "valid/instance_1.json")
      jsonld <- Future.successful(client.render(result.baseUnit, Mimes.`application/ld+json`))
      jsonLdUnit <- client.parseContent(jsonld, "application/ld+json")
    } yield {
      result.conforms shouldBe true
      jsonLdUnit.conforms shouldBe true
      jsonLdUnit.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      jsonLdUnit.sourceSpec shouldBe Spec.MCP
    }
  }
}
