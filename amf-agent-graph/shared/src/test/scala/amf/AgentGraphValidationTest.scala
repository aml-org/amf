package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentgraph.client.scala.{AgentGraphBaseUnitClient, AgentGraphConfiguration}
import org.scalatest.matchers.should.Matchers

class AgentGraphValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String                  = "file://amf-agent-graph/shared/src/test/resources/instances/"
  private val client: AgentGraphBaseUnitClient = AgentGraphConfiguration.AgentGraph().baseUnitClient()

  test("Valid AgentGraph YAML Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Valid AgentGraph Instance should conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid AgentGraph YAML Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }

  test("Invalid AgentGraph Instance should not conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }
}
