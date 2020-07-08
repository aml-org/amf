package amf.dialects

import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.remote.Cache
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.features.validation.AMFValidatorPlugin
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectInstancesValidationTest extends DialectInstanceValidation {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath       = "file://amf-client/shared/src/test/resources/vocabularies2/validation/"
  val productionPath = "file://amf-client/shared/src/test/resources/vocabularies2/production/"

  test("validation dialect 1 example 1 correct") {
    validate("dialect1.raml", "instance1_correct1.raml", 0)
  }

  test("validation of dialect with any type") {
    validate("any_dialect.raml", "any_example.raml", 0)
  }

  test("validation of dialect with any type array") {
    validate("any_array_dialect.raml", "any_array_example.raml", 0)
  }

  test("validation dialect 1b example 1b correct") {
    validate("dialect1b.raml", "example1b.raml", 0)
  }

  test("validation dialect 1 example 1 incorrect") {
    validate("dialect1.raml", "instance1_incorrect1.raml", 8)
  }

  test("validation dialect 2 example 1 correct") {
    validate("dialect2.raml", "instance2_correct1.raml", 0)
  }

  test("validation dialect 2 example 1 incorrect") {
    validate("dialect2.raml", "instance2_incorrect1.raml", 1)
  }

  test("validation dialect 3 example 1 correct") {
    validate("dialect3.raml", "instance3_correct1.raml", 0)
  }

  test("validation dialect 3 example 1 incorrect") {
    validate("dialect3.raml", "instance3_incorrect1.raml", 2)
  }

  test("validation dialect 4 example 1 correct") {
    validate("dialect4.raml", "instance4_correct1.raml", 0)
  }

  test("validation dialect 4 example 1 incorrect") {
    validate("dialect4.raml", "instance4_incorrect1.raml", 2)
  }

  test("validation dialect 5 example 1 correct") {
    validate("dialect5.raml", "instance5_correct1.raml", 0)
  }

  test("validation dialect 5 example 1 incorrect") {
    validate("dialect5.raml", "instance5_incorrect1.raml", 1)
  }

  test("validation dialect 6 example 1 correct") {
    validate("dialect6.raml", "instance6_correct1.raml", 0)
  }

  test("validation dialect 6 example 1 incorrect") {
    validate("dialect6.raml", "instance6_incorrect1.raml", 1)
  }

  test("validation dialect 7 example 1 correct") {
    validate("dialect7.raml", "instance7_correct1.raml", 0)
  }

  test("validation dialect 7 example 1 incorrect") {
    validate("dialect7.raml", "instance7_incorrect1.raml", 1)
  }

  test("validation dialect 8 example 1 correct") {
    validate("dialect8a.raml", "instance8_correct1.raml", 0)
  }

  test("validation dialect 8 example 1 incorrect") {
    validate("dialect8a.raml", "instance8_incorrect1.raml", 2)
  }

  test("validation dialect 9 example 1 correct") {
    validate("dialect9.raml", "instance9_correct1.raml", 0)
  }

  test("validation dialect 9 example 1 incorrect") {
    validate("dialect9.raml", "instance9_incorrect1.raml", 1)
  }

  test("validation dialect 10 example 1 correct") {
    validate("dialect10.raml", "instance10_correct1.raml", 0)
  }

  // TODO: un-ignore when AML re-implements the validation
  ignore("validation dialect 10 example 1 incorrect - Dialect ID in comment (instead of key)") {
    recoverToSucceededIf[Exception] { // Unknown type of dialect header
      validate("dialect10.raml", "instance10_incorrect1.raml", 0)
    }
  }

  // TODO: un-ignore when AML re-implements the validation
  ignore("validation dialect 10 example 2 incorrect - Dialect ID in both key and comment") {
    validate("dialect9.raml", "instance9_correct1.raml", 0).flatMap(_ =>
      validate("dialect10.raml", "instance10_incorrect2.raml", 2))
    // 1st error -> Dialect 9 defined in Header and Dialect 10 as key (validation and parse Dialect 9 as fallback)
    // 2nd error -> Dialect 9 does not accepts Dialect 10 key as a Root declaration
  }

  test("validation mule_config  example 1 correct") {
    validate("mule_config_dialect1.raml", "mule_config_instance_correct1.raml", 0)
  }

  test("validation mule_config  example 1 incorrect") {
    validate("mule_config_dialect1.raml", "mule_config_instance_incorrect1.raml", 1)
  }

  test("validation mule_config  example 2 incorrect") {
    validate("mule_config_dialect1.raml", "mule_config_instance_incorrect2.raml", 1)
  }

  test("validation eng_demos  example 1 correct") {
    validate("eng_demos_dialect1.raml", "eng_demos_instance1.raml", 0)
  }

  test("custom validation profile for dialect") {
    withCustomValidationProfile("eng_demos_dialect1.raml",
                                "eng_demos_instance1.raml",
                                ProfileName("eng_demos_profile.raml"),
                                "Custom Eng-Demos Validation",
                                4)
  }

  test("custom validation profile for dialect default profile") {
    withCustomValidationProfile("eng_demos_dialect1.raml",
                                "eng_demos_instance1.raml",
                                ProfileName("eng_demos_profile.raml"),
                                "Eng Demos 0.1",
                                0)
  }

  test("custom validation profile for ABOUT dialect default profile") {
    withCustomValidationProfile("ABOUT-dialect.raml",
                                "ABOUT.yaml",
                                ProfileName("ABOUT-validation.raml"),
                                "ABOUT-validation",
                                2,
                                productionPath + "ABOUT/")
  }

  test("Custom validation profile for ABOUT dialect default profile negative case") {
    withCustomValidationProfile("ABOUT-dialect.raml",
                                "ABOUT.custom.errors.yaml",
                                ProfileName("ABOUT-validation.raml"),
                                "ABOUT-validation",
                                4,
                                productionPath + "ABOUT/")
  }

  test("Can validate asyncapi 0.1 error") {
    validate("dialect1.raml", "example1.raml", 1, productionPath + "asyncapi/")
  }

  test("Can validate asyncapi 0.2 correct") {
    validate("dialect2.raml", "example2.raml", 0, productionPath + "asyncapi/")
  }

  test("Can validate asyncapi 0.3 correct") {
    validate("dialect3.raml", "example3.raml", 0, productionPath + "asyncapi/")
  }

  test("Can validate asyncapi 0.4 correct") {
    validate("dialect4.raml", "example4.raml", 0, productionPath + "asyncapi/")
  }

  test("Can validate container configurations") {
    validate("dialect.raml", "system.raml", expectedErrorCount = 0, productionPath + "system/")
  }

  test("Can validate oas 2.0 dialect instances") {
    validate("oas20_dialect1.yaml", "oas20_instance1.yaml", expectedErrorCount = 0, productionPath)
  }

  test("Can validate multiple property values with mapTermKey property") {
    validate("map-term-key.yaml", "map-term-key-instance.yaml", expectedErrorCount = 0)
  }

}
