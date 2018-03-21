package amf.dialects

import amf.core.AMFCompiler
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.features.validation.AMFValidatorPlugin
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectInstancesValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-client/shared/src/test/resources/vocabularies2/validation/"

  test("validation dialect 1 example 1 correct") {
    validate("dialect1.raml", "instance1_correct1.raml", 0)
  }

  test("validation dialect 1 example 1 incorrect") {
    validate("dialect1.raml", "instance1_incorrect1.raml", 6)
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
    validate("dialect3.raml", "instance3_incorrect1.raml", 1)
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
    customValidationProfile("eng_demos_dialect1.raml",
                            "eng_demos_instance1.raml",
                            "eng_demos_profile.raml",
                            "Custom Eng-Demos Validation",
                            6)
  }

  test("custom validation profile for dialect default profile") {
    customValidationProfile("eng_demos_dialect1.raml",
                            "eng_demos_instance1.raml",
                            "eng_demos_profile.raml",
                            "Eng Demos 0.1",
                            0)
  }

  protected def validate(dialect: String, instance: String, numErrors: Int) = {
    amf.core.AMF.registerPlugin(RAMLVocabulariesPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    for {
      _ <- amf.core.AMF.init()
      dialect <- {
        new AMFCompiler(
          basePath + dialect,
          platform,
          None,
          Some("application/yaml"),
          RAMLVocabulariesPlugin.ID
        ).build()
      }
      instance <- {
        AMFValidatorPlugin.enabled = true
        new AMFCompiler(
          basePath + instance,
          platform,
          None,
          Some("application/yaml"),
          RAMLVocabulariesPlugin.ID
        ).build()
      }
      report <- {
        RuntimeValidator(
          instance,
          dialect.asInstanceOf[Dialect].nameAndVersion()
        )
      }
    } yield {
      if (numErrors == 0) {
        if (!report.conforms)
          println(report)
        assert(report.conforms)
      } else assert(report.results.length == numErrors)
    }
  }

  protected def customValidationProfile(dialect: String,
                                        instance: String,
                                        profile: String,
                                        name: String,
                                        numErrors: Int) = {
    amf.core.AMF.registerPlugin(RAMLVocabulariesPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    for {
      _ <- amf.core.AMF.init()
      dialect <- {
        new AMFCompiler(
          basePath + dialect,
          platform,
          None,
          Some("application/yaml"),
          RAMLVocabulariesPlugin.ID
        ).build()
      }
      profile <- {
        AMFValidatorPlugin.enabled = true
        AMFValidatorPlugin.loadValidationProfile(basePath + profile)
      }
      instance <- {
        AMFValidatorPlugin.enabled = true
        new AMFCompiler(
          basePath + instance,
          platform,
          None,
          Some("application/yaml"),
          RAMLVocabulariesPlugin.ID
        ).build()
      }
      report <- {
        RuntimeValidator(
          instance,
          name
        )
      }
    } yield {
      if (numErrors == 0) {
        if (!report.conforms)
          println(report)
        assert(report.conforms)
      } else assert(report.results.length == numErrors)
    }
  }
}
