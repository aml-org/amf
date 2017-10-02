package amf.validation

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote.Syntax.Yaml
import amf.remote.{Raml, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import amf.validation.emitters.ValidationReportJSONLDEmitter
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

case class ExpectedReport(conforms: Boolean, numErrors: Integer, profile: String)

class ValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath         = "file://shared/src/test/resources/vocabularies/"
  val vocabulariesPath = "file://shared/src/test/resources/vocabularies/"
  val examplesPath     = "file://shared/src/test/resources/validations/"

  test("Loading and serializing validations") {
    val validation = Validation(platform)
    val expected   = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation
      .loadValidationDialect()
      .flatMap(
        unit =>
          AMFCompiler(basePath + "validation_profile_example.raml",
                      platform,
                      RamlYamlHint,
                      None,
                      None,
                      platform.dialectsRegistry)
            .build())
    actual
      .flatMap({ unit =>
        AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString
      })
      .zip(expected)
      .map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of encodes") {
    val validation = Validation(platform)
    val expected   = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation
      .loadValidationDialect()
      .flatMap(
        unit =>
          AMFCompiler(basePath + "validation_profile_example.raml",
                      platform,
                      RamlYamlHint,
                      None,
                      None,
                      platform.dialectsRegistry)
            .build())
    actual
      .flatMap(unit => new AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString)
      .zip(expected)
      .map(checkDiff)

  }

  test("Loading and serializing validations with inplace definition of range") {
    val validation = Validation(platform)
    val expected   = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation
      .loadValidationDialect()
      .flatMap(
        unit =>
          AMFCompiler(basePath + "validation_profile_example.raml",
                      platform,
                      RamlYamlHint,
                      None,
                      None,
                      platform.dialectsRegistry)
            .build())
    actual
      .flatMap(unit => new AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString)
      .zip(expected)
      .map(checkDiff)

  }
  test("Loading and serializing validations with union type") {
    val validation = Validation(platform)
    val expected   = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation
      .loadValidationDialect()
      .flatMap(
        unit =>
          AMFCompiler(basePath + "validation_profile_example.raml",
                      platform,
                      RamlYamlHint,
                      None,
                      None,
                      platform.dialectsRegistry)
            .build())
    actual
      .flatMap(unit => new AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString)
      .zip(expected)
      .map(checkDiff)

  }

  test("Load dialect") {
    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint).build()
      report <- validation.validate(model, ValidationProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.head.validationId == "http://raml.org/vocabularies/amf/parser#raml-schemes")
      assert(report.results.head.targetProperty.contains("http://raml.org/vocabularies/http#scheme"))
    }
  }

  test("Validation test, ignore profile") {

    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint).build()
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/error1_ignore_profile.raml")
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom function validation success test") {

    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint).build()
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_success.raml")
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom function validation failure test") {

    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint).build()
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_error.raml")
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.length == 1)
      assert(report.results.head.validationId == "http://raml.org/vocabularies/data#my_custom_validation")
    }
  }

  test("Validation test, custom validation profile") {

    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint).build()
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/error1_custom_validation_profile.raml")
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.length == 1)
      val result = report.results.head
      assert(result.level == "Info")
      assert(result.validationId == "http://raml.org/vocabularies/data#my-custom-validation")
      assert(result.targetNode == "file:/shared/src/test/resources/validations/data/error1.raml#/web-api")
      assert(result.targetProperty.get == "http://raml.org/vocabularies/http#scheme")
      assert(result.message == "error wadus")
      assert(result.position.isDefined)
    }
  }

  test("Validation report generation") {

    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint).build()
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_error.raml")
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(Option(ValidationReportJSONLDEmitter.emitJSON(report)).isDefined)
    }
  }

  test("Banking example validation") {
    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "banking/api.raml", platform, RamlYamlHint).build()
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "banking/profile.raml")
      report <- validation.validate(model, "Banking")
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 10)
    }
  }

  val testValidations = Map(
    "bad_domain/amf.jsonld"            -> ExpectedReport(conforms = false, 3, ValidationProfileNames.OAS),
    "bad_domain/valid.jsonld"          -> ExpectedReport(conforms = false, 1, ValidationProfileNames.OAS),
    "endpoint/amf.jsonld"              -> ExpectedReport(conforms = false, 1, ValidationProfileNames.AMF),
    "endpoint/valid.jsonld"            -> ExpectedReport(conforms = true, 0, ValidationProfileNames.AMF),
    "operation/amf.jsonld"             -> ExpectedReport(conforms = false, 1, ValidationProfileNames.AMF),
    "operation/valid.jsonld"           -> ExpectedReport(conforms = true, 0, ValidationProfileNames.AMF),
    "parameters/amf_properties.jsonld" -> ExpectedReport(conforms = false, 4, ValidationProfileNames.AMF),
    "parameters/amf_empty.jsonld"      -> ExpectedReport(conforms = false, 4, ValidationProfileNames.AMF),
    "parameters/amf_valid.jsonld"      -> ExpectedReport(conforms = true, 0, ValidationProfileNames.AMF),
    "shapes/enum_amf.jsonld"           -> ExpectedReport(conforms = false, 1, ValidationProfileNames.OAS),
    "shapes/enum_valid.jsonld"         -> ExpectedReport(conforms = true, 0, ValidationProfileNames.OAS),
    "webapi/amf.jsonld"                -> ExpectedReport(conforms = false, 2, ValidationProfileNames.OAS),
    "webapi/valid.jsonld"              -> ExpectedReport(conforms = false, 1, ValidationProfileNames.OAS),
    "webapi/valid.jsonld"              -> ExpectedReport(conforms = true, 0, ValidationProfileNames.RAML),
    "webapi/bad_protocol.jsonld"       -> ExpectedReport(conforms = false, 1, ValidationProfileNames.RAML)
  )

  for {
    (file, expectedReport) <- testValidations
  } yield {
    test(s"SHACL Validator $file") {
      validate(file, expectedReport)
    }
  }

  private def validate(file: String, expectedReport: ExpectedReport) = {
    platform.resolve(examplesPath + file, None).flatMap { data =>
      val model                = data.stream.toString
      val validation           = Validation(platform)
      val effectiveValidations = validation.computeValidations(expectedReport.profile)
      val shapes               = validation.shapesGraph(effectiveValidations)
      platform.validator.report(
        model,
        "application/ld+json",
        shapes,
        "application/ld+json"
      ) flatMap { report =>
        assert(expectedReport == ExpectedReport(report.conforms, report.results.length, expectedReport.profile))
      }
    }
  }
}
