package amf.validation

import amf.ProfileNames
import amf.common.Tests.checkDiff
import amf.core.client.GenerationOptions
import amf.core.model.document.{Document, Module}
import amf.core.model.domain.{DataNode, RecursiveShape, Shape}
import amf.core.remote.Syntax.{Json, Yaml}
import amf.core.remote._
import amf.core.unsafe.{PlatformSecrets, TrunkPlatform}
import amf.core.validation.SeverityLevels
import amf.facades.{AMFCompiler, AMFDumper, Validation}
import amf.plugins.document.graph.parser.GraphEmitter
import amf.plugins.document.webapi.RAML10Plugin
import amf.plugins.document.webapi.validation.{
  AnnotationsValidation,
  ExamplesValidation,
  PayloadValidation,
  ShapeFacetsValidation,
  _
}
import amf.plugins.domain.shapes.models.{ArrayShape, NodeShape}
import amf.plugins.features.validation.PlatformValidator
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter
import org.scalatest.AsyncFunSuite
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

case class ExpectedReport(conforms: Boolean, numErrors: Integer, profile: String)

class ValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath         = "file://amf-client/shared/src/test/resources/vocabularies/"
  val vocabulariesPath = "file://amf-client/shared/src/test/resources/vocabularies/"
  val examplesPath     = "file://amf-client/shared/src/test/resources/validations/"
  val payloadsPath     = "file://amf-client/shared/src/test/resources/payloads/"
  val productionPath   = "file://amf-client/shared/src/test/resources/production/"
  val validationsPath  = "file://amf-client/shared/src/test/resources/validations/"

  test("Loading and serializing validations") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    val actual: Future[String] = validation.loadValidationDialect() flatMap { _ =>
      AMFCompiler(basePath + exampleFile, platform, RamlYamlHint, Validation(platform), None, None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("prefixes can be loaded") {
    val expectedFile             = "validation_profile_prefixes.raml.jsonld"
    val exampleFile              = "validation_profile_prefixes.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    val actual: Future[String] = validation.loadValidationDialect() flatMap { _ =>
      AMFCompiler(basePath + exampleFile, platform, RamlYamlHint, Validation(platform), None, None).build()
    } map { AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Prefixes can be parsed") {
    val expectedFile             = "validation_profile_prefixes.raml"
    val exampleFile              = "validation_profile_prefixes.raml.jsonld"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    val actual: Future[String] = validation.loadValidationDialect() flatMap { _ =>
      AMFCompiler(basePath + exampleFile, platform, AmfJsonHint, Validation(platform), None, None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of encodes") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    val actual: Future[String] = validation.loadValidationDialect() flatMap { _ =>
      AMFCompiler(basePath + exampleFile, platform, RamlYamlHint, Validation(platform), None, None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of range") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val actual: Future[String] = validation.loadValidationDialect() flatMap { _ =>
      AMFCompiler(basePath + exampleFile, platform, RamlYamlHint, Validation(platform), None, None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with union type") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val actual: Future[String] = validation.loadValidationDialect() flatMap { _ =>
      AMFCompiler(basePath + exampleFile, platform, RamlYamlHint, Validation(platform), None, None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Load dialect") {
    val validation = Validation(platform)
    for {
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
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
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/error1_ignore_profile.raml")
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom function validation success test") {
    val validation = Validation(platform)
    for {
      //_      <- validation.loadValidationDialect()
      //_      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_success.raml")
      model  <- AMFCompiler(basePath + "mule_config.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, "RAML 1.0 Vocabulary")
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 3)
    }
  }

  test("Raml Vocabulary") {
    val validation = Validation(platform)
    for {
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_success.raml")
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom function validation failure test") {
    val validation = Validation(platform)
    for {
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_error.raml")
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
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
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/error1_custom_validation_profile.raml")
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, "Test Profile")
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
    val validation = Validation(platform)
    for {
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "data/custom_function_validation_error.raml")
      model  <- AMFCompiler(examplesPath + "data/error1.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, "Test Profile")
    } yield {
      assert(Option(ValidationReportJSONLDEmitter.emitJSON(report)).isDefined)
    }
  }

  test("Banking example validation") {
    val validation = Validation(platform)
    for {
      _      <- validation.loadValidationDialect()
      _      <- validation.loadValidationProfile(examplesPath + "banking/profile.raml")
      model  <- AMFCompiler(examplesPath + "banking/api.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, "Banking")
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 10)
      assert(report.results.nonEmpty)
    }
  }

  test("Library example validation") {
    val validation = Validation(platform)
    for {
      _      <- validation.loadValidationDialect()
      model  <- AMFCompiler(examplesPath + "library/nested.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Closed shapes validation") {
    val validation = Validation(platform)
    for {
      _      <- validation.loadValidationDialect()
      model  <- AMFCompiler(examplesPath + "closed_nodes/api.raml", platform, RamlYamlHint, validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 6)
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
      val pair = for {
        library <- AMFCompiler(payloadsPath + "payloads.raml", platform, RamlYamlHint, Validation(platform)).build()
        payload <- AMFCompiler(payloadsPath + payloadFile, platform, hint, Validation(platform)).build()
      } yield {
        val targetType = library
          .asInstanceOf[Module]
          .declares
          .find {
            case s: Shape => s.name == shapeName
          }
          .get
        (PayloadValidation(platform, targetType.asInstanceOf[Shape]), payload)
      }

      pair flatMap {
        case (validation, payload) =>
          validation.validate(payload.asInstanceOf[Document].encodes.asInstanceOf[DataNode])
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
      content <- platform.resolve(payloadsPath + "b_valid.yaml", None)
      filePayload <- AMFCompiler(payloadsPath + "b_valid.yaml", platform, PayloadYamlHint, Validation(platform))
        .build()
      textPayload <- AMFCompiler(payloadsPath + "b_valid.yaml",
                                 TrunkPlatform(content.stream.toString),
                                 PayloadYamlHint,
                                 Validation(platform)).build()
    } yield {
      val fileJson = JsonRender.render(GraphEmitter.emit(filePayload, GenerationOptions()))
      val textJson = JsonRender.render(GraphEmitter.emit(textPayload, GenerationOptions()))
      assert(fileJson == textJson)
    }

  }

  val testValidations = Map(
    "bad_domain/amf.jsonld"                   -> ExpectedReport(conforms = false, 2, ProfileNames.OAS),
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
    platform.resolve(examplesPath + file, None).flatMap { data =>
      val validation           = Validation(platform)
      val model                = data.stream.toString
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

  test("Example validations test") {
    for {
      library <- AMFCompiler(examplesPath + "examples_validation.raml", platform, RamlYamlHint, Validation(platform))
        .build()
      results <- ExamplesValidation(library, platform).validate()
    } yield {
      assert(results.length == 4)
      assert(results.count(_.level == SeverityLevels.WARNING) == 1)
    }
  }

  test("Example model validation test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "examples_validation.raml", platform, RamlYamlHint, validation).build()
      report  <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 4)
      assert(report.results.count(_.level == SeverityLevels.WARNING) == 1)
    }
  }

  test("Shape facets validations test") {
    for {
      library <- AMFCompiler(examplesPath + "facets/custom-facets.raml", platform, RamlYamlHint, Validation(platform))
        .build()
      results <- ShapeFacetsValidation(library, platform).validate()
    } yield {
      assert(results.length == 1)
    }
  }

  test("Shape facets model validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "facets/custom-facets.raml", platform, RamlYamlHint, validation).build()
      report  <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 5)
    }
  }

  test("Annotations validations test") {
    for {
      library <- AMFCompiler(examplesPath + "annotations/annotations.raml",
                             platform,
                             RamlYamlHint,
                             Validation(platform))
        .build()
      results <- AnnotationsValidation(library, platform).validate()
    } yield {
      assert(results.length == 1)
    }
  }

  test("Annotations enum validations test") {
    for {
      library <- AMFCompiler(examplesPath + "annotations/annotations_enum.raml",
                             platform,
                             RamlYamlHint,
                             Validation(platform))
        .build()
      results <- AnnotationsValidation(library, platform).validate()
    } yield {
      assert(results.length == 4)
    }
  }

  test("Duplicated endpoints validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "endpoint/duplicated.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("Invalid baseUri validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "webapi/invalid_baseuri.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("Mutually exclusive 'type' and 'schema' facets validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "types/exclusive_facets.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 2)
    }
  }

  test("Mutually exclusive 'types' and 'schemas' facets validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "webapi/exclusive_facets.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("Valid baseUri validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "webapi/valid_baseuri.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Example validation of a resource type") {
    val validation = Validation(platform)
    for {
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
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "annotations/annotations.raml", platform, RamlYamlHint, validation).build()
      report  <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
    }
  }

  test("Example of object validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "examples/object-name-example.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Example min and max constraint validations test") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(examplesPath + "examples/max-min-constraint.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test js custom validation - multiple of") {
    val validation = Validation(platform)
    for {
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
    val validation = Validation(platform)
    for {
      _       <- validation.loadValidationDialect()
      library <- AMFCompiler(examplesPath + "libraries/api.raml", platform, RamlYamlHint, validation).build()
      _       <- validation.loadValidationProfile(examplesPath + "libraries/profile.raml")
      report  <- validation.validate(library, "Test")
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Can parse and validate a complex recursive API") {
    val validation = Validation(platform) //
    for {
      library <- AMFCompiler(productionPath + "getsandbox.comv1swagger.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(Option(report).isDefined)
    }
  }

  test("Can parse a recursive API") {
    val validation = Validation(platform)
    for {
      doc <- AMFCompiler(productionPath + "recursive.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved = RAML10Plugin.resolve(doc)
      assert(Option(resolved).isDefined)
    }
  }

  test("Can parse a recursive array API") {
    val validation = Validation(platform)
    for {
      doc <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved      = RAML10Plugin.resolve(doc)
      val A: ArrayShape = resolved.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      assert(A.items.isInstanceOf[RecursiveShape])
      assert(A.items.name == "items")
      val AOrig   = doc.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      val profile = new AMFShapeValidations(AOrig).profile()
      assert(profile != null)
    }
  }

  test("Can normalize a recursive array API") {
    val validation = Validation(platform)
    for {
      doc <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val A: ArrayShape = doc.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      val profile       = new AMFShapeValidations(A).profile()
      assert(profile.violationLevel.size == 2)
      assert(
        profile.violationLevel.head == "file://amf-client/shared/src/test/resources/production/recursive2.raml#/declarations/array/A_validation")
      assert(
        profile.violationLevel.last == "file://amf-client/shared/src/test/resources/production/recursive2.raml#/declarations/array/A_recursive_validation")
    }
  }

  ignore("Some api") {
    val validation = Validation(platform)
    for {
      library <- AMFCompiler(productionPath + "enum-inheritance.raml", platform, RamlYamlHint, validation).build()
      report  <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(library != null)
      assert(report.results.length == 1)
    }
  }
}
