package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agenticnetwork.client.scala.{AgenticNetworkBaseUnitClient, AgenticNetworkConfiguration}
import org.scalatest.matchers.should.Matchers

class AgenticNetworkValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String                  = "file://amf-agentic-network/shared/src/test/resources/instances/"
  private val client: AgenticNetworkBaseUnitClient = AgenticNetworkConfiguration.AgenticNetwork().baseUnitClient()

  test("Valid AgenticNetwork YAML Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Valid AgenticNetwork Instance should conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid AgenticNetwork YAML Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }

  test("Invalid AgenticNetwork Instance should not conform with sync validate") {
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
