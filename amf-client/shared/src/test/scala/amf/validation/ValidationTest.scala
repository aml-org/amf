package amf.validation

import amf.ProfileNames
import amf.common.Tests.checkDiff
import amf.core.client.GenerationOptions
import amf.core.model.document.{Document, Module}
import amf.core.model.domain.{DataNode, RecursiveShape, Shape}
import amf.core.remote.Syntax.{Json, Syntax, Yaml}
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
import amf.plugins.domain.shapes.models.ArrayShape
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

  private def cycle(exampleFile: String, hint: Hint, syntax: Syntax, target: Vendor): Future[String] = {
    Validation(platform).flatMap(v => {
      v.loadValidationDialect().map(_ => v)
    }) flatMap { v =>
      AMFCompiler(basePath + exampleFile, platform, hint, v, None, None).build()
    } map {
      AMFDumper(_, target, syntax, GenerationOptions()).dumpToString
    }
  }

  test("Loading and serializing validations") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("prefixes can be loaded") {
    val expectedFile             = "validation_profile_prefixes.raml.jsonld"
    val exampleFile              = "validation_profile_prefixes.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, RamlYamlHint, Json, Amf).zip(expected).map(checkDiff)
  }

  test("Prefixes can be parsed") {
    val expectedFile             = "validation_profile_prefixes.raml"
    val exampleFile              = "validation_profile_prefixes.raml.jsonld"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, AmfJsonHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of encodes") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of range") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with union type") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
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

  test("Custom function validation success test") {
    for {
      validation <- Validation(platform)
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
        validation <- Validation(platform).map(_.withEnabledValidation(false))
        library    <- AMFCompiler(payloadsPath + "payloads.raml", platform, RamlYamlHint, validation).build()
        payload    <- AMFCompiler(payloadsPath + payloadFile, platform, hint, validation).build()
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
      content    <- platform.resolve(payloadsPath + "b_valid.yaml", None)
      validation <- Validation(platform).map(_.withEnabledValidation(false))
      filePayload <- AMFCompiler(payloadsPath + "b_valid.yaml", platform, PayloadYamlHint, validation)
        .build()
      validationPayload <- Validation(platform).map(_.withEnabledValidation(false))
      textPayload <- AMFCompiler(payloadsPath + "b_valid.yaml",
                                 TrunkPlatform(content.stream.toString),
                                 PayloadYamlHint,
                                 validationPayload).build()
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
      results <- ExamplesValidation(library, platform).validate()
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

  test("Shape facets validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "facets/custom-facets.raml", platform, RamlYamlHint, validation)
        .build()
      results <- ShapeFacetsValidation(library, platform).validate()
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
      results <- AnnotationsValidation(library, platform).validate()
    } yield {
      assert(results.length == 1)
    }
  }

  test("Annotations enum validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "annotations/annotations_enum.raml", platform, RamlYamlHint, validation)
        .build()
      results <- AnnotationsValidation(library, platform).validate()
    } yield {
      assert(results.length == 4)
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

  test("Mutually exclusive 'type' and 'schema' facets validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "types/exclusive_facets.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 2)
    }
  }

  test("Mutually exclusive 'types' and 'schemas' facets validations test") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(examplesPath + "webapi/exclusive_facets.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      report.results.foreach(result => assert(result.position.isDefined))
      assert(report.results.length == 1)
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

  test("Can parse a recursive array API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
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
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
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

  test("Type inheritance with enum") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "enum-inheritance.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  test("Some production api with includes") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "includes-api/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.size == 1) // TODO: Check the example that is failing here, gray area
    }
  }

  test("Library with includes") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "library/with-include/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.results.isEmpty)
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
}
