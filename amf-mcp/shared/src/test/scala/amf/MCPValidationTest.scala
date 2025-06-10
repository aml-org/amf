package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.mcp.client.scala.{MCPBaseUnitClient, MCPConfiguration}
import org.scalatest.matchers.should.Matchers

class MCPValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String          = "file://amf-mcp/shared/src/test/resources/instances/"
  private val client: MCPBaseUnitClient = MCPConfiguration.MCP().baseUnitClient()

  test("Valid MCP Instance should conforms") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid MCP Instance should not conforms") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }

  test("Valid MCP Instance should conforms with sync validate") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.json")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid MCP Instance should not conforms with sync validate") {
    for {
      parseResult <- client.parse(basePath + "invalid/invalid_instance_1.json")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }
}
