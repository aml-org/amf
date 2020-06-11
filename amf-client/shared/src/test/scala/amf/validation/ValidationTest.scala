package amf.validation

import _root_.org.scalatest.AsyncFunSuite
import amf._
import amf.client.parse.DefaultParserErrorHandler
import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.SeverityLevels
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.Raml10Plugin
import amf.plugins.features.validation.CoreValidations

import scala.concurrent.{ExecutionContext, Future}

case class ExpectedReport(
    conforms: Boolean,
    numErrors: Integer,
    profile: ProfileName,
    jsNumErrors: Option[Integer] = None) // todo: we should remove this, both platforms should validate the same

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

  //what is speciy testing?? should be partitioned in a some new of tests? extract to tckUtor?
  ignore("Trailing spaces validation") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(productionPath + "americanflightapi.raml",
                             platform,
                             RamlYamlHint,
                             eh = DefaultParserErrorHandler.withRun()).build()
      report <- validation.validate(library, RamlProfile)
    } yield {
      assert(report.conforms)
    }
  }

  // this is not a validation test
  ignore("Can parse a recursive API") {
    val eh = DefaultParserErrorHandler.withRun()

    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive.raml", platform, RamlYamlHint, eh = eh).build()
    } yield {
      val resolved = Raml10Plugin.resolve(doc, eh)
      assert(Option(resolved).isDefined)
    }
  }

  // is testing that the api has no errors. Should be in Platform?
  test("Some production api with includes") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(productionPath + "includes-api/api.raml",
                             platform,
                             RamlYamlHint,
                             eh = DefaultParserErrorHandler.withRun()).build()
      report <- validation.validate(library, RamlProfile)
    } yield {
      val (violations, others) =
        report.results.partition(r => r.level.equals(SeverityLevels.VIOLATION))
      assert(violations.isEmpty)
      assert(others.lengthCompare(1) == 0)
      assert(others.head.level == SeverityLevels.WARNING)
      assert(others.head.message.equals("'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"))
    }
  }

  // tck examples?! for definition this name its wrong. What it's testing? the name makes reference to an external fragment exception, but the golden its a normal and small api.
  test("Test validate external fragment cast exception") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/tck-examples/cast-external-exception.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(report.conforms)
    }
  }

  // the reported null pointer case could not be reproduced. This test was added with the whole api to prove that there is any null pointer.
  // should we delete this case?
  test("Raml 0.8 Null pointer tck case APIMF-429") {

    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/tck-examples/nullpointer-spec-example.raml",
                             platform,
                             RamlYamlHint,
                             eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(library, Raml08Profile)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  // this is a real case, recursion in json schema??
  test("Test stackoverflow case from Platform") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/stackoverflow/api.raml",
                             platform,
                             RamlYamlHint,
                             eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(library, RamlProfile)
    } yield {
      assert(!report.results.exists(_.validationId != CoreValidations.RecursiveShapeSpecification.id))
    }
  }

  // same than the previous one
  test("Test stackoverflow case 0.8 from Platform") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/stackoverflow2/api.raml",
                             platform,
                             RamlYamlHint,
                             eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(library, Raml08Profile)
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
// why the generation???? Move to MovelValidationReportTest?
  test("Security scheme and traits test") {
    val eh = DefaultParserErrorHandler.withRun()
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/securitySchemes/security1.raml", platform, RamlYamlHint, eh = eh)
        .build()
      resolved <- Future {
        Raml10Plugin.resolve(doc, eh)
      }
      generated <- new AMFSerializer(resolved, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
      report    <- validation.validate(doc, RamlProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 2)
      assert(report.results.exists(_.message.contains("Security scheme 'undefined' not found in declarations.")))
    }
  }

  test("Custom validaton problems 1") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/missing-annotation-types/api.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      resolved <- Future {
        Raml10Plugin.resolve(doc, doc.errorHandler())
      }
      generated <- new AMFSerializer(resolved, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
      report    <- validation.validate(doc, RamlProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Custom validation problems 2 (RAML)") {
    val eh = DefaultParserErrorHandler.withRun()

    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/enumeration-arrays/api.raml", platform, RamlYamlHint, eh = eh)
        .build()
      resolved <- Future {
        Raml10Plugin.resolve(doc, eh)
      }
      generated <- new AMFSerializer(resolved, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
      report    <- validation.validate(doc, RamlProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Custom validation problems 2 (OAS)") {
    val eh = DefaultParserErrorHandler.withRun()
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/enumeration-arrays/api.raml", platform, RamlYamlHint, eh = eh)
        .build()
      resolved <- Future {
        Raml10Plugin.resolve(doc, eh)
      }
      generated <- new AMFSerializer(resolved, "application/ld+json", Amf.name, RenderOptions().withoutSourceMaps).renderToString
      report    <- validation.validate(doc, OasProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("Matrix tests") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/types/arrays/matrix_type_expression.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Patterned properties tests") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/types/patterned_properties.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Recursive array test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/types/recursive_array.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("oas example error in shape becomes warning") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/production/oas_example1.yaml",
                         platform,
                         OasYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, Oas20Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Null super-array items test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/null_superarray_items/api.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("0.8 'id' identifiers in JSON Schema shapes test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/id_json_schema_locations.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 2)
    }
  }

  test("Erroneous JSON schema in ResourceType test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/resource_type_failing_schema/api.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, RamlProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Nested XML Schema test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/nested_xml_schema/api.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, Raml10Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Recursion introduced after resource type application test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/recursion_after_resource_type/api.raml",
                         platform,
                         RamlYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, Raml08Profile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Numeric status codes in OAS responses") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/oas_numeric_resources.yaml",
                         platform,
                         OasYamlHint,
                         eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- validation.validate(doc, Oas20Profile)
    } yield {
      assert(report.conforms)
    }
  }

  ignore("emilio performance") {
    for {
      validation <- Validation(platform)
      // Path should point to the main api file.
      model <- AMFCompiler(
        productionPath + "sys-sabre-air-api-1.0.3-fat-raml/ha-sys-sabre-air-api.raml",
        platform,
        RamlYamlHint,
        eh = DefaultParserErrorHandler.withRun()) // Change hint here for a different syntax parsing.
        .build()
      report <- validation.validate(model, RamlProfile) // Change profile name here to validate for a different spec.
    } yield {
      //RAML10Plugin.resolve(model) // Change plugin here to resolve for a different spec.
      assert(report.results.isEmpty)
    }
    //assert(true)
  }
}
