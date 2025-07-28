package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentsdomain.client.scala.{AgentsDomainBaseUnitClient, AgentsDomainConfiguration}
import org.scalatest.matchers.should.Matchers

class AgentsDomainValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String                  = "file://amf-agents-domain/shared/src/test/resources/instances/"
  private val client: AgentsDomainBaseUnitClient = AgentsDomainConfiguration.AgentsDomain().baseUnitClient()

  test("Valid AgentsDomain JSON Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Valid AgentsDomain YAML Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid AgentsDomain JSON Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 2
    }
  }

  test("Invalid AgentsDomain YAML Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 2
    }
  }

  test("Valid AgentsDomain Instance should conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.json")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid AgentsDomain Instance should not conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "invalid/invalid_instance_1.json")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 2
    }
  }
}
