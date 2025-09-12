package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.llmmetadata.client.scala.{LLMMetadataBaseUnitClient, LLMMetadataConfiguration}
import org.scalatest.matchers.should.Matchers

class LLMMetadataValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String                  = "file://amf-llm-metadata/shared/src/test/resources/instances/"
  private val client: LLMMetadataBaseUnitClient = LLMMetadataConfiguration.LLMMetadata().baseUnitClient()

  test("Valid LLMMetadata JSON Instance should conform") {
    for {
      parseResult      <- client.parse(basePath + "valid/instance_1.json")
      validationReport <- client.validate(parseResult.baseUnit)
    } yield {
      parseResult.conforms shouldBe true
      validationReport.conforms shouldBe true
    }
  }
//
//  test("Valid LLMMetadata YAML Instance should conform") {
//    for {
//      parseResult      <- client.parse(basePath + "valid/instance_1.yaml")
//      validationReport <- client.validate(parseResult.baseUnit)
//    } yield {
//      parseResult.conforms shouldBe true
//      validationReport.conforms shouldBe true
//    }
//  }
//
//  test("Invalid LLMMetadata JSON Instance should not conform") {
//    for {
//      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.json")
//      validationReport <- client.validate(parseResult.baseUnit)
//    } yield {
//      parseResult.conforms shouldBe true
//      validationReport.conforms shouldBe false
//      validationReport.results.size shouldBe 7
//    }
//  }
//
//  test("Invalid LLMMetadata YAML Instance should not conform") {
//    for {
//      parseResult      <- client.parse(basePath + "invalid/invalid_instance_1.yaml")
//      validationReport <- client.validate(parseResult.baseUnit)
//    } yield {
//      parseResult.conforms shouldBe true
//      validationReport.conforms shouldBe false
//      validationReport.results.size shouldBe 7
//    }
//  }
//
//  test("Valid LLMMetadata Instance should conform with sync validate") {
//    for {
//      parseResult <- client.parse(basePath + "valid/instance_1.json")
//      validationReport = client.syncValidate(parseResult.baseUnit)
//    } yield {
//      parseResult.conforms shouldBe true
//      validationReport.conforms shouldBe true
//    }
//  }
//
//  test("Invalid LLMMetadata Instance should not conform with sync validate") {
//    for {
//      parseResult <- client.parse(basePath + "invalid/invalid_instance_1.json")
//      validationReport = client.syncValidate(parseResult.baseUnit)
//    } yield {
//      parseResult.conforms shouldBe true
//      validationReport.conforms shouldBe false
//      validationReport.results.size shouldBe 7
//    }
//  }
}
