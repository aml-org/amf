package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentmetadata.client.scala.{AgentMetadataBaseUnitClient, AgentMetadataConfiguration}
import org.scalatest.matchers.should.Matchers

class AgentMetadataValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String                  = "file://amf-agent-metadata/shared/src/test/resources/instances/"
  private val client: AgentMetadataBaseUnitClient = AgentMetadataConfiguration.AgentMetadata().baseUnitClient()

  test("Valid AgentMetadata JSON Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Valid AgentMetadata YAML Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid AgentMetadata JSON Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }

  test("Invalid AgentMetadata YAML Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 1
    }
  }

  test("Valid AgentMetadata Instance should conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.json")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid AgentMetadata Instance should not conform with sync validate") {
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
