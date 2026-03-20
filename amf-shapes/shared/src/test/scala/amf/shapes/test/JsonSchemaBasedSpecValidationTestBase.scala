package amf.shapes.test

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient
import org.scalatest.matchers.should.Matchers

abstract class JsonSchemaBasedSpecValidationTestBase
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with Matchers {

  def testConfig: JsonSchemaBasedSpecTestConfig

  private lazy val basePath = "file://" + testConfig.basePath
  private lazy val client   = testConfig.configuration.baseUnitClient().asInstanceOf[JsonSchemaBasedSpecBaseUnitClient]

  testConfig.validInstances.foreach { instance =>
    test(s"Valid ${testConfig.specName} instance $instance should conform") {
      for {
        parseResult      <- client.parse(basePath + instance)
        validationReport <- client.validate(parseResult.baseUnit)
      } yield {
        parseResult.conforms shouldBe true
        validationReport.conforms shouldBe true
      }
    }

    test(s"Valid ${testConfig.specName} instance $instance should conform with sync validate") {
      for {
        parseResult <- client.parse(basePath + instance)
      } yield {
        val validationReport = client.syncValidate(parseResult.baseUnit)
        parseResult.conforms shouldBe true
        validationReport.conforms shouldBe true
      }
    }
  }

  testConfig.invalidInstances.foreach { case InvalidInstance(path, expectedErrors) =>
    test(s"Invalid ${testConfig.specName} instance $path should not conform") {
      for {
        parseResult      <- client.parse(basePath + path)
        validationReport <- client.validate(parseResult.baseUnit)
      } yield {
        parseResult.conforms shouldBe true
        validationReport.conforms shouldBe false
        expectedErrors.fold(succeed) { count =>
          validationReport.results.size shouldBe count
        }
      }
    }

    test(s"Invalid ${testConfig.specName} instance $path should not conform with sync validate") {
      for {
        parseResult <- client.parse(basePath + path)
      } yield {
        val validationReport = client.syncValidate(parseResult.baseUnit)
        parseResult.conforms shouldBe true
        validationReport.conforms shouldBe false
        expectedErrors.fold(succeed) { count =>
          validationReport.results.size shouldBe count
        }
      }
    }
  }
}
