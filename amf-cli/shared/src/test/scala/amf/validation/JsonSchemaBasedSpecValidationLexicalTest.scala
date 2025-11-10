package amf.validation

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.mcp.client.scala.{MCPBaseUnitClient, MCPConfiguration}
import org.scalatest.matchers.should.Matchers

class JsonSchemaBasedSpecValidationLexicalTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  // This suite is mean to test the lexical in general for JsonSchemaBasedSpecs. I choose to do it in MCP because is the easier one
  private val basePath: String = "file://amf-mcp/shared/src/test/resources/instances/invalid/lexical/"
  private val client: MCPBaseUnitClient = MCPConfiguration.MCP().baseUnitClient()

  test("Invalid MCP with missing required property at root") {
    for {
      parseResult <- client.parse(basePath + "invalid-root-required.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
      validationReport.results.head.position.isDefined shouldBe true
      validationReport.results.head.location.isDefined shouldBe true
      validationReport.results.head.position.get.value shouldBe "[(1,0)-(7,0)]"
    }
  }

  test("Invalid MCP with missing required property at array") {
    for {
      parseResult <- client.parse(basePath + "invalid-array-required.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
      validationReport.results.head.position.isDefined shouldBe true
      validationReport.results.head.location.isDefined shouldBe true
      validationReport.results.head.position.get.value shouldBe "[(18,4)-(31,0)]"
    }
  }

  test("Invalid MCP with missing required inner property") {
    for {
      parseResult <- client.parse(basePath + "invalid-property-required.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
      validationReport.results.head.position.isDefined shouldBe true
      validationReport.results.head.location.isDefined shouldBe true
      validationReport.results.head.position.get.value shouldBe "[(7,0)-(8,0)]"
    }
  }

  test("Invalid MCP with invalid type") {
    for {
      parseResult <- client.parse(basePath + "invalid-type.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
      validationReport.results.head.position.isDefined shouldBe true
      validationReport.results.head.location.isDefined shouldBe true
      validationReport.results.head.position.get.value shouldBe "[(2,14)-(7,1)]"
    }
  }

  test("Invalid MCP with invalid additional property") {
    for {
      parseResult <- client.parse(basePath + "invalid-additional.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
      validationReport.results.head.position.isDefined shouldBe true
      validationReport.results.head.location.isDefined shouldBe true
      validationReport.results.head.position.get.value shouldBe "[(1,0)-(11,0)]"
    }
  }
}
