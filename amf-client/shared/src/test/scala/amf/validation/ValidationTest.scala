package amf.validation

import amf.ProfileNames
import amf.common.Tests.checkDiff
import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
import amf.core.model.document.{Document, Module, PayloadFragment}
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.remote.Syntax.{Json, Syntax, Yaml}
import amf.core.remote._
import amf.core.services.PayloadValidator
import amf.core.unsafe.{PlatformSecrets, TrunkPlatform}
import amf.core.validation.{SeverityLevels, ValidationCandidate}
import amf.facades.{AMFCompiler, AMFRenderer, Validation}
import amf.plugins.document.graph.parser.GraphEmitter
import amf.plugins.document.webapi.RAML10Plugin
import amf.plugins.document.webapi.validation.{AMFShapeValidations, PayloadValidation, UnitPayloadsValidation}
import amf.plugins.domain.shapes.models.ArrayShape
import amf.plugins.domain.webapi.models.WebApi
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter
import amf.plugins.features.validation.{ParserSideValidations, PlatformValidator}
import org.scalatest.AsyncFunSuite
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

case class ExpectedReport(conforms: Boolean, numErrors: Integer, profile: String)

class ValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath         = "file://amf-client/shared/src/test/resources/vocabularies2/production/validation/"
  val vocabulariesPath = "file://amf-client/shared/src/test/resources/vocabularies2/production/validation/"
  val examplesPath     = "file://amf-client/shared/src/test/resources/validations/"
  val payloadsPath     = "file://amf-client/shared/src/test/resources/payloads/"
  val productionPath   = "file://amf-client/shared/src/test/resources/production/"
  val validationsPath  = "file://amf-client/shared/src/test/resources/validations/"
  val upDownPath       = "file://amf-client/shared/src/test/resources/upanddown/"
  val parserPath       = "file://amf-client/shared/src/test/resources/org/raml/parser/"
  val jsonSchemaPath   = "file://amf-client/shared/src/test/resources/validations/jsonschema"

  private def cycle(exampleFile: String, hint: Hint, syntax: Syntax, target: Vendor): Future[String] = {
    Validation(platform).flatMap(v => {
      v.loadValidationDialect().map(_ => v)
    }) flatMap { v =>
      AMFCompiler(basePath + exampleFile, platform, hint, v, None, None).build()
    } flatMap {
      AMFRenderer(_, target, syntax, RenderOptions()).renderToString
    }
  }

  test("Loading and serializing validations") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("prefixes can be loaded") {
    val expectedFile             = "validation_profile_prefixes.raml.jsonld"
    val exampleFile              = "validation_profile_prefixes.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, RamlYamlHint, Json, Amf).zip(expected).map(checkDiff)
  }

  test("Prefixes can be parsed") {
    val expectedFile             = "validation_profile_prefixes.raml"
    val exampleFile              = "validation_profile_prefixes.raml.jsonld"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, AmfJsonHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of encodes") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of range") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with union type") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Load dialect") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.head.validationId == "http://raml.org/vocabularies/amf/parser#raml-schemes")
      assert(report.results.head.targetProperty.contains("http://raml.org/vocabularies/http#scheme"))
    }
  }

  test("Validation test, ignore profile") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "data/error1_ignore_profile.raml")
      model      <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Raml Vocabulary") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_success.raml")
      model      <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom function validation failure test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_error.raml")
      model      <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.length == 1)
      assert(report.results.head.validationId == "http://raml.org/vocabularies/data#my_custom_validation")
    }
  }

  test("Validation test, custom validation profile") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "data/error1_custom_validation_profile.raml")
      model      <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.length == 1)

      val result = report.results.head
      assert(result.level == "Info")
      assert(result.validationId == "http://raml.org/vocabularies/data#my-custom-validation")
      assert(result.targetNode == "file://amf-client/shared/src/test/resources/validations/data/error1.raml#/web-api")
      assert(result.targetProperty.get == "http://raml.org/vocabularies/http#scheme")
      assert(result.message == "error wadus")
      assert(result.position.isDefined)
    }
  }

  test("Validation report generation") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_error.raml")
      model      <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "Test Profile")
    } yield {
      assert(Option(ValidationReportJSONLDEmitter.emitJSON(report)).isDefined)
    }
  }

  test("Banking example validation") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "banking/profile.raml")
      model      <- AMFCompiler(examplesPath + "banking/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "Banking")
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 10)
      assert(report.results.nonEmpty)
    }
  }

  test("Library example validation") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(examplesPath + "library/nested.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Closed shapes validation") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(examplesPath + "closed_nodes/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 6)
    }
  }

  test("Array minCount 1") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(productionPath + "arrayItems1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Array minCount 2") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(productionPath + "arrayItems2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("JSON Schema allOf test1") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/allOf/api1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 3)
    }
  }

  test("JSON Schema allOf test2") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/allOf/api2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 4)
    }
  }

  test("JSON Schema allOf test3") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/allOf/api3.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema anyOf test1") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/anyOf/api1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema anyOf test2") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/anyOf/api2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("JSON Schema anyOf test3") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/anyOf/api3.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema oneOf test1") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/oneOf/api1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("JSON Schema oneOf test2") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/oneOf/api2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("JSON Schema oneOf test3") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/oneOf/api3.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("JSON Schema not test1") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/not/api1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema not test2") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/not/api2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("JSON Schema not test3") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/not/api3.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema not test4") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/not/api4.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema ref test1") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/ref/api1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("JSON Schema ref test2") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/ref/api2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema ref test3") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/ref/api3.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema ref test4") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/ref/api4.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("JSON Schema ref test5") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/ref/api5.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  ignore("JSON Schema ref test6") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(jsonSchemaPath + "/ref/api6.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  val payloadValidations = Map(
    ("A", "a_valid.json")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("A", "a_invalid.json")               -> ExpectedReport(conforms = false, 4, "Payload"),
    ("B", "b_valid.json")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("B", "b_invalid.json")               -> ExpectedReport(conforms = false, 1, "Payload"),
    ("B", "b_valid.yaml")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("B", "b_invalid.yaml")               -> ExpectedReport(conforms = false, 1, "Payload"),
    ("C", "c_valid.json")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("C", "c_invalid.json")               -> ExpectedReport(conforms = false, 8, "Payload"),
    ("D", "d_valid.json")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("D", "d_invalid.json")               -> ExpectedReport(conforms = false, 7, "Payload"),
    ("E", "e_valid.json")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("E", "e_invalid.json")               -> ExpectedReport(conforms = false, 1, "Payload"),
    ("F", "f_valid.json")                 -> ExpectedReport(conforms = true, 0, "Payload"),
    ("F", "f_invalid.json")               -> ExpectedReport(conforms = false, 1, "Payload"),
    ("G", "g1_valid.json")                -> ExpectedReport(conforms = true, 0, "Payload"),
    ("G", "g2_valid.json")                -> ExpectedReport(conforms = true, 0, "Payload"),
    ("G", "g_invalid.json")               -> ExpectedReport(conforms = false, 1, "Payload"),
    ("H", "h_invalid.json")               -> ExpectedReport(conforms = false, 1, "Payload"),
    ("PersonData", "person_valid.yaml")   -> ExpectedReport(conforms = true, 0, "Payload"),
    ("PersonData", "person_invalid.yaml") -> ExpectedReport(conforms = false, 2, "Payload")
  )

  for {
    ((shapeName, payloadFile), expectedReport) <- payloadValidations
  } yield {
    test(s"SHACL Payload Validator $payloadFile") {
      val hint = payloadFile.split("\\.").last match {
        case "json" => PayloadJsonHint
        case "yaml" => PayloadYamlHint
      }
      val validation: Future[PayloadValidation] = for {
        validation <- Validation(platform).map(_.withEnabledValidation(false))
        library    <- AMFCompiler(payloadsPath + "payloads.raml", platform, RamlYamlHint, validation).build()
        payload    <- AMFCompiler(payloadsPath + payloadFile, platform, hint, validation).build()
      } yield {
        val targetType = library
          .asInstanceOf[Module]
          .declares
          .find {
            case s: Shape => s.name.is(shapeName)
          }
          .get
        val candidates =
          Seq(ValidationCandidate(targetType.asInstanceOf[Shape], payload.asInstanceOf[PayloadFragment]))
        PayloadValidation(candidates)
      }

      validation flatMap {
        _ validate ()
      } map { report =>
        report.results.foreach { result =>
          assert(result.position.isDefined)
        }
        assert(report.conforms == expectedReport.conforms)
        assert(report.results.length == expectedReport.numErrors)
      }
    }
  }

  test("payload parsing test") {
    for {
      content    <- platform.resolve(payloadsPath + "b_valid.yaml")
      validation <- Validation(platform).map(_.withEnabledValidation(false))
      filePayload <- AMFCompiler(payloadsPath + "b_valid.yaml", platform, PayloadYamlHint, validation)
        .build()
      validationPayload <- Validation(platform).map(_.withEnabledValidation(false))
      textPayload <- AMFCompiler(payloadsPath + "b_valid.yaml",
                                 TrunkPlatform(content.stream.toString),
                                 PayloadYamlHint,
                                 validationPayload).build()
    } yield {
      val fileJson = JsonRender.render(GraphEmitter.emit(filePayload, RenderOptions()))
      val textJson = JsonRender.render(GraphEmitter.emit(textPayload, RenderOptions()))
      assert(fileJson == textJson)
    }

  }

  test("param validation") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "/production/oas_data.json", platform, OasJsonHint, validation)
        .build()
      result <- {
        val stringShape = model
          .asInstanceOf[Document]
          .encodes
          .asInstanceOf[WebApi]
          .endPoints
          .head
          .operations
          .head
          .request
          .headers
          .head
          .schema
        PayloadValidator.validate(stringShape, "2015-07-20T21:00:00", SeverityLevels.VIOLATION)
      }
    } yield {
      assert(result.conforms)
    }

  }

  val testValidations = Map(
    "bad_domain/valid.jsonld"                 -> ExpectedReport(conforms = true, 0, ProfileNames.OAS),
    "endpoint/amf.jsonld"                     -> ExpectedReport(conforms = false, 1, ProfileNames.AMF),
    "endpoint/valid.jsonld"                   -> ExpectedReport(conforms = true, 0, ProfileNames.AMF),
    "operation/amf.jsonld"                    -> ExpectedReport(conforms = false, 1, ProfileNames.AMF),
    "operation/valid.jsonld"                  -> ExpectedReport(conforms = true, 0, ProfileNames.AMF),
    "parameters/amf_properties.jsonld"        -> ExpectedReport(conforms = false, 4, ProfileNames.AMF),
    "parameters/amf_empty.jsonld"             -> ExpectedReport(conforms = false, 4, ProfileNames.AMF),
    "parameters/amf_valid.jsonld"             -> ExpectedReport(conforms = true, 0, ProfileNames.AMF),
    "shapes/enum_amf.jsonld"                  -> ExpectedReport(conforms = false, 2, ProfileNames.OAS),
    "shapes/enum_valid.jsonld"                -> ExpectedReport(conforms = true, 0, ProfileNames.OAS),
    "webapi/amf.jsonld"                       -> ExpectedReport(conforms = false, 1, ProfileNames.OAS),
    "webapi/valid.jsonld"                     -> ExpectedReport(conforms = false, 1, ProfileNames.OAS),
    "webapi/valid.jsonld"                     -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "webapi/bad_protocol.jsonld"              -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "types/scalars/missing_type.jsonld"       -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "types/scalars/missing_type_valid.jsonld" -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/scalars/wrong_facet.jsonld"        -> ExpectedReport(conforms = false, 2, ProfileNames.RAML),
    "types/scalars/valid_facet.jsonld"        -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/arrays/wrong_items.jsonld"         -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "types/arrays/right_items.jsonld"         -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/arrays/empty_items.jsonld"         -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/arrays/empty_items.jsonld"         -> ExpectedReport(conforms = false, 1, ProfileNames.OAS),
    "annotationTypes/invalid.jsonld"          -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "annotationTypes/valid.jsonld"            -> ExpectedReport(conforms = true, 0, ProfileNames.RAML)
  )

  for {
    (file, expectedReport) <- testValidations
  } yield {
    test(s"SHACL Validator $file") {
      validate(file, expectedReport)
    }
  }

  private def validate(file: String, expectedReport: ExpectedReport) = {
    platform.resolve(examplesPath + file).flatMap { data =>
      val model = data.stream.toString
      Validation(platform).flatMap { validation =>
        val effectiveValidations = validation.computeValidations(expectedReport.profile)
        val shapes               = validation.shapesGraph(effectiveValidations)
        PlatformValidator.instance.report(
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

  test("Example validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "examples_validation.raml", platform, RamlYamlHint, validation)
        .build()
      results <- UnitPayloadsValidation(library, platform).validate()
    } yield {
      assert(results.length == 4)
      assert(results.count(_.level == SeverityLevels.WARNING) == 1)
    }
  }

  test("Example model validation test") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(examplesPath + "examples_validation.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 4)
      assert(report.results.count(_.level == SeverityLevels.WARNING) == 1)
    }
  }

  test("Duplicated title property test") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(validationsPath + "webapi/dup_title.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.head.message.contains("Property 'title' is duplicated"))
    }
  }

  test("Validates example1.raml") {
    for {
      validation <- Validation(platform)
      document   <- AMFCompiler(validationsPath + "production/example1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(document, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Shape facets validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "facets/custom-facets.raml", platform, RamlYamlHint, validation)
        .build()
      results <- UnitPayloadsValidation(library, platform).validate()
    } yield {
      assert(results.length == 1)
    }
  }

  test("Shape facets model validations test") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(examplesPath + "facets/custom-facets.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 4)
    }
  }

  test("Annotations validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "annotations/annotations.raml", platform, RamlYamlHint, validation)
        .build()
      results <- UnitPayloadsValidation(library, platform).validate()
    } yield {
      assert(results.length == 1)
    }
  }

  test("Annotations enum validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "annotations/annotations_enum.raml", platform, RamlYamlHint, validation)
        .build()
      results <- UnitPayloadsValidation(library, platform).validate()
    } yield {
      assert(results.length == 2)
    }
  }

  test("Duplicated endpoints validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "endpoint/duplicated.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("Invalid baseUri validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "webapi/invalid_baseuri.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  ignore("Invalid mediaType validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "webapi/invalid_media_type.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("MinLength, maxlength facets validations test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(examplesPath + "types/lengths.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      val (violations, warnings) = report.results.partition(r => r.level.equals(SeverityLevels.VIOLATION))
      assert(violations.lengthCompare(1) == 0)
    }
  }

  test("big numbers validations test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(examplesPath + "types/big_nums.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      val (violations, warnings) = report.results.partition(r => r.level.equals(SeverityLevels.VIOLATION))
      assert(violations.lengthCompare(1) == 0)
    }
  }

  test("Mutually exclusive 'type' and 'schema' facets validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "types/exclusive_facets.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      val (violations, warnings) = report.results.partition(r => r.level.equals(SeverityLevels.VIOLATION))
      assert(violations.lengthCompare(2) == 0)
    }
  }

  test("Mutually exclusive 'types' and 'schemas' facets validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "webapi/exclusive_facets.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      val (violations, warnings) = report.results.partition(r => r.level.equals(SeverityLevels.VIOLATION))
      report.results.foreach(result => assert(result.position.isDefined))
      assert(violations.lengthCompare(1) == 0)
    }
  }

  test("Valid baseUri validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "webapi/valid_baseuri.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("No title validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "no_title.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Spec usage examples example validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "spec_examples_example.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test Issue Nil validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "/testIssueNil/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Nil value validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(validationsPath + "nil_validation.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Property overwriting") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "property_overwriting.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.head.level == SeverityLevels.VIOLATION)
    }
  }

  test("Trailing spaces validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "americanflightapi.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid media type") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "invalid_media_type.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Example validation of a resource type") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "resource_types/resource_type1.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Annotations model validations test") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(examplesPath + "annotations/annotations.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("Example of object validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "examples/object-name-example.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Example min and max constraint validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "examples/max-min-constraint.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("json schema inheritance") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(upDownPath + "schema_inheritance.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.size == 1)
      assert(report.results.head.level == SeverityLevels.WARNING)
      assert(report.conforms)
    }
  }

  test("xml schema inheritance") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(upDownPath + "schema_inheritance2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Example invalid min and max constraint validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "examples/invalid-max-min-constraint.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.lengthCompare(6) == 0)
    }
  }

  test("Test js custom validation - multiple of") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "/custom-js-validations/mutiple-of.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)

    }
  }

  ignore("Example JS library validations") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      library    <- AMFCompiler(examplesPath + "libraries/api.raml", platform, RamlYamlHint, validation).build()
      _          <- validation.loadValidationProfile(examplesPath + "libraries/profile.raml")
      report     <- validation.validate(library, "Test")
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Can parse and validate a complex recursive API") {
    for {
      validation <- Validation(platform) //
      library <- AMFCompiler(productionPath + "getsandbox.comv1swagger.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(Option(report).isDefined)
    }
  }

  test("Can parse a recursive API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved = RAML10Plugin.resolve(doc)
      assert(Option(resolved).isDefined)
    }
  }

  test("Can validate an array example") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "wrong_example1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Null trait API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "null_trait.raml", platform, RamlYamlHint, validation).build()
      generated  <- new AMFSerializer(doc, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.results.size == 1)
      assert(report.results.head.level == "Warning")
      assert(report.conforms)
    }
  }

  test("Can parse a recursive array API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved      = RAML10Plugin.resolve(doc)
      val A: ArrayShape = resolved.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      assert(A.items.isInstanceOf[RecursiveShape])
      assert(A.items.name.is("items"))
      val AOrig   = doc.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      val profile = new AMFShapeValidations(AOrig).profile()
      assert(profile != null)
    }
  }

  test("Can parse the production financial api") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/financial-api/infor-financial-api.raml",
                         platform,
                         RamlYamlHint,
                         validation).build()
      // TODO: FIXME! problem serialising to JSON
      // generated <- new AMFSerializer(doc, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
    } yield {
      val resolved = RAML10Plugin.resolve(doc)
      assert(Option(resolved).isDefined)
    }
  }

  test("Can normalize a recursive array API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val A: ArrayShape = doc.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      val profile       = new AMFShapeValidations(A).profile()
      assert(profile.violationLevel.size == 1)
      assert(
        profile.violationLevel.head == "file://amf-client/shared/src/test/resources/production/recursive2.raml#/declarations/array/A_validation")
    }
  }

  test("Type inheritance with enum") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "enum-inheritance.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("External raml 0.8 fragment") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "08/external_fragment_test.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Param in raml 0.8 api") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "08/pattern.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Validation error raml 0.8 example 1") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "08/validation_error1.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Some production api with includes") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "includes-api/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      val (violations, others) =
        report.results.partition(r => r.level.equals(SeverityLevels.VIOLATION))
      assert(violations.isEmpty)
      assert(others.lengthCompare(1) == 0)
      assert(others.head.level == SeverityLevels.WARNING)
      assert(others.head.message.equals("'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"))
    }
  }

  test("Library with includes") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "library/with-include/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.results.exists(_.validationId != ParserSideValidations.RecursiveShapeSpecification.id()))
    }
  }

  test("Max length validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "shapes/max-length.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.size == 1)
    }
  }

  test("Min length validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "shapes/min-length.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.size == 1)
    }
  }

  test("Headers example array validation") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "production/headers.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Exclusive example vs examples validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/example_examples.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(
        report.results.exists(
          _.message.contains("Properties 'example' and 'examples' are exclusive and cannot be declared together")))
      assert(!report.conforms)
    }
  }

  test("Exclusive queryString vs queryParameters validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/query_string_parameters.raml",
                         platform,
                         RamlYamlHint,
                         validation).build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(
        report.results.exists(_.message.contains(
          "Properties 'queryString' and 'queryParameters' are exclusive and cannot be declared together")))
      assert(!report.conforms)
    }
  }

  test("Annotation target usage") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "annotations/target-annotations.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Spec extension") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "extends/extension.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Spec overlay 1") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "extends/overlay1.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Spec overlay 2") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "extends/overlay2.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Spec resource type fragment") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "resource_types/fragment.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("08 Validation") {

    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "08/some.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Test validate pattern with valid example") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "examples/pattern-valid.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test validate pattern with invalid example") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "examples/pattern-invalid.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Test validate union ex 1 with valid example a)") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "examples/union1a-valid.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test validate union ex 1 with valid example b)") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "examples/union1b-valid.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test failed union ex 1 with invalid example") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "examples/union1-invalid.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Test validate external fragment cast exception") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/tck-examples/cast-external-exception.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Raml 0.8 Parameter") {

    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/tck-examples/query-parameter.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Raml 0.8 Query Parameter Negative test case") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/08/date-query-parameter.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Raml 0.8 Query Parameter Positive test case") {

    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/08/date-query-parameter-correct.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Raml 0.8 Null pointer tck case APIMF-429") {

    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/tck-examples/nullpointer-spec-example.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("JSON API Validation positive case") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "jsonapi/jsonapi_profile.raml")
      model      <- AMFCompiler(examplesPath + "jsonapi/correct.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "JSON API")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("JSON API Validation negative case") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      _          <- validation.loadValidationProfile(examplesPath + "jsonapi/jsonapi_profile.raml")
      model      <- AMFCompiler(examplesPath + "jsonapi/incorrect.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, "JSON API 1.0")
    } yield {
      assert(report.results.length == 51)
      assert(!report.conforms)
    }
  }

  test("Test for different examples") {

    val validation = Validation(platform)
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/tck-examples/examples.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Empty parameter validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/08/empty-param.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Date parameter validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/08/empty-param.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Empty payload with example validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/08/empty-payload-with-example.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Invalid yaml with scalar an map as value") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/expanded-inheritance-with-example.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("float numeric constraints") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/floats.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 4)
    }
  }

  test("Shape with items in oas") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/shape-with-items.json", platform, OasJsonHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.OAS)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Tags in oas") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/webapi/tags.json", platform, OasJsonHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.OAS)
    } yield {
      val results = report.results.filter(_.level == SeverityLevels.VIOLATION)
      assert(results.lengthCompare(1) == 0)
      assert(results.head.message.contains("Tag must have a name"))
    }
  }

  test("Invalid example validation over union shapes") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/invalid-example-in-unions.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
      val results = report.results.filter(_.level == SeverityLevels.VIOLATION)
      assert(results.lengthCompare(1) == 0)
      assert(results.head.message.contains("Data at / must be one of the valid union types"))
    }
  }

  test("Invalid example no media types") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/examples/example-no-media-type.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
      val results = report.results.filter(_.level == SeverityLevels.VIOLATION)
      assert(results.length == 2)
      assert(results.exists(_.message.contains("Invalid media type")))
    }
  }

  test("Valid examples validation over union shapes") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/examples-in-unions.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Valid examples validation over union shapes 2") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/unions_examples.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid example in any shape") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/shapes/any-shape-invalid-example.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
      assert(report.results.lengthCompare(1) == 0)
      assert(report.results.head.level == SeverityLevels.WARNING)
    }
  }

  test("Test stackoverflow case from Platform") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/stackoverflow/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.results.exists(_.validationId != ParserSideValidations.RecursiveShapeSpecification.id()))
    }
  }

  test("Test stackoverflow case 0.8 from Platform") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/stackoverflow2/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Test out of range status code") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/webapi/invalid_status_code.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.exists(_.message.contains("Status code must be numeric and in the 1xx-5xx range")))
      assert(!report.conforms)
    }
  }

  test("Security scheme and traits test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/securitySchemes/security1.raml", platform, RamlYamlHint, validation)
        .build()
      resolved <- Future {
        RAML10Plugin.resolve(doc)
      }
      generated <- new AMFSerializer(resolved, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
      report    <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 4)
      assert(report.results.exists(_.message.contains("Security scheme 'undefined' not found in declarations.")))

    }
  }

  test("Chained references violation test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/chained/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.exists(_.message.contains("Chained reference")))
    }
  }

  test("Test empty string in title") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/webapi/invalid_title1.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.exists(_.message.contains("API name must not be an empty string")))
      assert(!report.conforms)
    }
  }

  test("Test resource type invalid examples args validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(productionPath + "/parameterized-references/input.raml",
                             platform,
                             RamlYamlHint,
                             validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.length == 4)
      assert(report.results.exists(_.message.contains("must be within")))
      assert(!report.conforms)
    }
  }

  test("Test resource type non string scalar parameter example") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(parserPath + "resource-types/non-string-scalar-parameter/input.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid key in trait test") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(validationsPath + "/traits/trait1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.exists(_.message.contains("Nested endpoint in trait")))
    }
  }

  test("Mandatory RAML documentation properties test") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(validationsPath + "/documentation/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
      assert(report.results.exists(_.message.contains("documentation item")))
    }
  }

  test("Invalid path template syntax text") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/unbalanced_paths.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
      assert(report.results.exists(_.message.contains("Invalid path template syntax")))
    }
  }

  test("Test minItems maxItems examples") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(examplesPath + "/examples/min-max-items.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
      assert(report.results.exists(_.message.contains("Number of items at")))
    }
  }

  test("Test validate declared type with two uses") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(examplesPath + "/examples/declared-type-ref.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.head.message.trim.equals(
        "Object at / must be valid\nScalar at //name must have data type http://www.w3.org/2001/XMLSchema#string\nScalar at //lastName must have data type http://www.w3.org/2001/XMLSchema#string"))
    }
  }

  test("Test declared type with two uses adding example") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(examplesPath + "/examples/declared-type-ref-add-example.raml",
                         platform,
                         RamlYamlHint,
                         validation).build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
      assert(!report.results.head.targetNode.equals(report.results.last.targetNode))
    }
  }

  test("Test unsupported example with raml08 profile") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(examplesPath + "/examples/unsupported-examples-08.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
      assert(report.results.length == 2)
      assert(report.results.filter(_.level == SeverityLevels.WARNING).lengthCompare(2) == 0)
    }
  }

  test("Test minimum maximum constraint between facets") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "/facets/min-max-between.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.exists(_.message.contains("Maximum must be greater than or equal to minimum")))
    }
  }

  test("Test minItems maxItems constraint between facets") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/facets/min-max-items-between.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.exists(_.message.contains("MaxItems must be greater than or equal to minItems")))
    }
  }

  test("Test minLength maxLength constraint between facets") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/facets/min-max-length-between.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.exists(_.message.contains("MaxLength must be greater than or equal to minLength")))
    }
  }

  test("Test validation of body with only example (default any shape)") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(examplesPath + "/examples/only-example-body.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Empty library entry") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/libraries/empty-library-entry.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Empty type ref") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "/types/empty-type-ref.yaml", platform, OasYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 2)
    }
  }

  test("Date times examples test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "date_time_validations.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(report.conforms)
    }
  }

  test("Example xml with sons results test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(examplesPath + "/xmlexample/offices_xml_type.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(report.conforms)
      assert(report.results.count(_.level == SeverityLevels.WARNING) == 3) // all warnings
    }
  }

  test("Invalid type example 1 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Invalid type example 2 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Invalid type example 3 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex3.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Invalid type example 4 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex4.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Invalid type example 5 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex5.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Invalid type example 6 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex6.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Invalid type example 7 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "invalidex7.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Valid type example 1 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "validex1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(report.conforms)
    }
  }

  test("Valid type example 2 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "validex2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid parameter binding") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "parameters/invalid-parameter-binding.json",
                         platform,
                         OasYamlHint,
                         validation).build()
      report <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 2)
    }
  }

  test("Invalid body parameter count") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "parameters/invalid-body-parameter.json", platform, OasYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(doc != null)
      // This is because the default payload has the same id.
      assert(report.results.count(_.level == SeverityLevels.VIOLATION) == 1)
    }
  }

  test("Test validate trait with quoted string example variable") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(upDownPath + "trait-string-quoted-node.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test maxProperties and minProperties constraint between facets") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/facets/min-max-properties-between.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
      assert(report.results.exists(_.message.contains("MaxProperties must be greater than or equal to minProperties")))
    }
  }

  test("Test maxProperties and minProperties constraints example") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/examples/min-max-properties-example.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
      assert(report.results.exists(_.message.contains("Expected max properties")))
      assert(report.results.exists(_.message.contains("Expected min properties")))
    }
  }

  test("Test examples in oas") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/examples/examples-in-oas.json", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.OAS, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 4)
    }
  }

  test("Test not stackoverflow when exists target to unresolved shapes") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(upDownPath + "/sapi-notification-saas-1.0.0-raml/api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Test validate headers in request") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/parameters/request-header.json", platform, OasJsonHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.OAS, ProfileNames.OAS)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test non existing include in type def") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/missing-includes/in-type-def.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Test non existing include in resource type def") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/missing-includes/in-resource-type-def.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("Test non existing include in trait def") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/missing-includes/in-trait-def.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("Test validate multiple tags") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/multiple-tags.json", platform, OasJsonHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.OAS, ProfileNames.OAS)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test properties with special names") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "property-names.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test enum number in string format validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/enum-number-string/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test array withouth item type validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/array-without-items.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.lengthCompare(1) == 0)
      assert(report.results.head.message.equals("Syntax error, generating empty array"))
    }
  }

  test("Test invalid map in resource type use") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/invalid-map-resource-type.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test variable not implemented in resource type use") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/variable-not-implemented-resourcetype.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.lengthCompare(1) == 0)
      assert(report.results.head.message.equals("Cannot find variable 'errorItem'."))
    }
  }

  test("Test media type with + char in resource type") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/media-type-resource-type.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test validation with # in property shape name") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/invalid-char-property-name.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("baseUriParameters without baseUri") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/no-base-uri.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(report.results.length == 2)
    }
  }

  test("Test multiple formData parameters") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "parameters/multiple-formdata.yaml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.OAS, ProfileNames.OAS)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid security scheme") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "invalid-security.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML08)
    } yield {
      assert(report.results.length == 1)
    }
  }

  test("Invalid type def with json schemas includes") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/invalid-jsonschema-includes/cloudhub-api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.results.length == 1)
      assert(report.results.head.message.equals("Cannot parse JSON Schema expression out of a non string value"))
    }
  }

  test("Numeric key in external fragment root entry") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/numeric-key-in-external-fragment/api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid library and type def in 08") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/invalid-lib-and-type-08/api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 2)
      assert(report.results.exists(_.message.equals("Property uses not supported in a raml 0.8 webApi node")))
      assert(report.results.exists(_.message.equals("Invalid type def duTypes.storyCollection for raml 08")))
    }
  }

  test("Invalid library tag type def") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/invalid-lib-tagtype/api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.exists(_.message.equals("Missing library location")))
    }
  }

  // Strange problem where hashcode for YMap entries had to be recalculated inside syaml.
  // Just check it doesn't throw NPE :)
  test("Null in type name") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "null-name.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.head.message.equals("Expecting !!str and !!null provided"))
    }
  }

  test("Exclusive Maximum Schema") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "08/max-exclusive-schema.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML08)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.exists(_.message.equals("Data at / must be smaller than to 180")))
    }
  }

  test("Validate json schema with non url id.") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "master-data---current-api-2.0.1-fat-raml/currencyapi.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("spi-viewer-api production example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(productionPath + "spi-viewer-api-1.0.0-fat-raml/spi-viewer-api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

}
