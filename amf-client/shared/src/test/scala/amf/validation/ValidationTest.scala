package amf.validation

import amf.ProfileNames
import amf.core.AMFSerializer
import amf.core.benchmark.ExecutionLog
import amf.core.emitter.RenderOptions
import amf.core.model.document.Module
import amf.core.model.domain.{ObjectNode, RecursiveShape}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.SeverityLevels
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.plugins.document.webapi.validation.AMFShapeValidations
import amf.plugins.document.webapi.{RAML08Plugin, RAML10Plugin}
import amf.plugins.domain.shapes.models.ArrayShape
import amf.plugins.features.validation.ParserSideValidations
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

case class ExpectedReport(conforms: Boolean, numErrors: Integer, profile: String)

class ValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath         = "file://amf-client/shared/src/test/resources/vocabularies2/production/validation/"
  val vocabulariesPath = "file://amf-client/shared/src/test/resources/vocabularies2/production/validation/"
  val examplesPath     = "file://amf-client/shared/src/test/resources/validations/"
  val productionPath   = "file://amf-client/shared/src/test/resources/production/"
  val validationsPath  = "file://amf-client/shared/src/test/resources/validations/"
  val upDownPath       = "file://amf-client/shared/src/test/resources/upanddown/"
  val parserPath       = "file://amf-client/shared/src/test/resources/org/raml/parser/"
  val jsonSchemaPath   = "file://amf-client/shared/src/test/resources/validations/jsonschema"

  // todo serialize json of validation report?
  // Example validations test and Example model validation test were the same, because the resolution runs always for validation

  // generic examples test? Extracted from spec? is not testing a particular cases, but testing different examples. This should be an unit test?
  test("Spec usage examples example validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "spec_examples_example.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
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

  //this shoulnot be an unit test
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

  // this is not a validation test
  test("Can parse a recursive API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved = RAML10Plugin.resolve(doc)
      assert(Option(resolved).isDefined)
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
      val AOrig   = doc.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      val profile = new AMFShapeValidations(AOrig).profile(ObjectNode())
      assert(profile != null)
    }
  }

  test("Can parse the production financial api") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/financial-api/infor-financial-api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
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
      new ValidationResolutionPipeline(ProfileNames.RAML).resolve(Module().withDeclares(Seq(A)))
      val profile = new AMFShapeValidations(A).profile(ObjectNode())
      assert(profile.violationLevel.size == 1)
      assert(
        profile.violationLevel.head == "file://amf-client/shared/src/test/resources/production/recursive2.raml#/declarations/types/array/A_validation")
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
      assert(!report.results.exists(_.validationId != ParserSideValidations.RecursiveShapeSpecification.id))
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
      assert(report.results.size == 3)
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
      assert(!report.results.exists(_.validationId != ParserSideValidations.RecursiveShapeSpecification.id))
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
      assert(report.results.size == 2)
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

  test("Date times invalid examples test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "date_time_validations2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, ProfileNames.AMF)
    } yield {
      assert(!report.conforms)
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

  test("Test array without item type validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/array-without-items.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
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
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.head.message.startsWith("'baseUri' not defined and 'baseUriParameters' defined."))
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
      model <- AMFCompiler(productionPath + "spi-viewer-api-1.0.0-fat-raml/spi-viewer-api.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
      assert(!report.results.exists(_.level != SeverityLevels.WARNING))
    }
  }

  test("survey-system production example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "survey-system-api-1.0.0-fat-raml/api.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.exists(_.message.equals("Cannot parse data node from AST structure '?'")))
    }
  }

  test("servicenow production example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "servicenow-system-api1-1.0.0-fat-raml/incident-api.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("salesforce-outbound example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "salesforce-outbound-api-batchv2-1.0.0-fat-raml/api.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("myconnect example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "myconnect-1.0.0-fat-raml/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("pattern raml example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(validationsPath + "08/ramlpattern.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("gmc-services-api--training example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "gmc-services-api---training-1.0.0-fat-raml/api.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("lock-unlock example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "lock-unlock-api-1.0.0-fat-raml/lockUnlockStats.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("case00182429 example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "case00182429-1.0.0-fat-raml/DarwinIntegration.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("security scheme authorizationGrant RAML 1.0") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(validationsPath + "securitySchemes/raml10AuthorizationGrant.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.exists(_.message.contains("Invalid authorization grant")))
    }
  }

  test("security scheme authorizationGrant RAML 0.8") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(validationsPath + "securitySchemes/raml08AuthorizationGrant.raml",
                           platform,
                           RamlYamlHint,
                           validation).build()
      report <- validation.validate(model, ProfileNames.RAML08)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.exists(_.message.contains("Invalid authorization grant")))
    }
  }

  test("Invalid map key") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "map-key.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(
        report.results.exists(_.message.equals("Property {alpha2code: } not supported in a raml 1.0 webApi node")))
    }
  }

  test("Pattern properties key") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "data/pattern_properties.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Pattern properties key 2 (all additional properties)") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "data/pattern_properties2.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Pattern properties key 3 (precedence)") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "data/pattern_properties3.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  ignore("Pattern properties key 4 (additionalProperties: false clash)") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "data/pattern_properties4.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }
  test("Include twice same json schema and add example in raml 08") {

    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/reuse-json-schema/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
    }
  }

  test("JSON Schema pattern properties") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "jsonSchemaProperties.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("vuconnectionapi example") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(productionPath + "vuconnectapi-1.0.0-fat-raml/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.nonEmpty)
    }
  }

  test("Json example external that starts with space") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/json-example-space-start/api.raml",
                         platform,
                         RamlYamlHint,
                         validation)
        .build()
      report <- validation.validate(doc, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
    }
  }

  test("Discriminator in union definition") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "discriminator_union.raml", platform, RamlYamlHint, validation)
        .build()
      report   <- validation.validate(doc, ProfileNames.RAML08)
      resolved <- Future { RAML08Plugin.resolve(doc) }
    } yield {
      assert(!report.conforms)
    }
  }

  test("empty example") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "08/empty_example.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Date format not SYaml timestamp") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "types/mhra-e-payment-v1.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("JSON Schema Draft-3 required property support") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "jsonschema/misc_shapes.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Connect and trace methods") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "connect-trace.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
    }
  }

  test("Empty responses") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "empty-responses.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test recursive optional shape") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "recursive-optional-property.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  ignore("emilio performance") {
    for {
      validation <- Validation(platform)
      // Path should point to the main api file.
      model <- AMFCompiler(productionPath + "sys-sabre-air-api-1.0.3-fat-raml/ha-sys-sabre-air-api.raml",
                           platform,
                           RamlYamlHint,
                           validation) // Change hint here for a different syntax parsing.
        .build()
      report <- validation.validate(model, ProfileNames.RAML) // Change profile name here to validate for a different spec.
    } yield {
      //RAML10Plugin.resolve(model) // Change plugin here to resolve for a different spec.
      assert(report.results.isEmpty)
    }
    //assert(true)
  }

  test("Examples JSON-Schema") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "08/examples-json-schema.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
    }
  }
  test("Recursive property") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "recursive-property.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.head.message == "Error recursive shape")
    }
  }

  test("Test valid recursive union recursive") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "shapes/union-recursive.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test more than one variable with link node in trait") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "traits/two-included-examples.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test different declarations with same name") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "declarations/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML08)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test empty usage/uses entries") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "empty-usage-uses.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, ProfileNames.RAML)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test web artifact process api") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(productionPath + "web-artifact-process-api-1.0.0-fat-raml/WebArtifactAPI.raml",
                           platform,
                           RamlYamlHint,
                           validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      println(report)
      assert(report.conforms)
    }
  }
  /*
  test("HERE_HERE test field_nation") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(productionPath + "field-nation-v2-api-2.0.7-fat-raml/FN_API_full.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, ProfileNames.RAML)
    } yield {
      ExecutionLog.finish()
      ExecutionLog.buildReport()
      assert(report.conforms)
    }
  }
 */
}
