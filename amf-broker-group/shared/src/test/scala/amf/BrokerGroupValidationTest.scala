package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.brokergroup.client.scala.{BrokerGroupBaseUnitClient, BrokerGroupConfiguration}
import org.scalatest.matchers.should.Matchers

class BrokerGroupValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String                  = "file://amf-broker-group/shared/src/test/resources/instances/"
  private val client: BrokerGroupBaseUnitClient = BrokerGroupConfiguration.BrokerGroup().baseUnitClient()

  test("Valid BrokerGroup JSON Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Valid BrokerGroup YAML Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid BrokerGroup JSON Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 2
    }
  }

  test("Invalid BrokerGroup YAML Instance should not conform") {
    for {
      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe false
      validationReport.results.size shouldBe 2
    }
  }

  test("Valid BrokerGroup Instance should conform with sync validate") {
    for {
      parseResult <- client.parse(basePath + "valid/instance_1.json")
      validationReport = client.syncValidate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }

  test("Invalid BrokerGroup Instance should not conform with sync validate") {
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
