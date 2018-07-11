package amf.validation

import amf._
import amf.core.AMFSerializer
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
import _root_.org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

case class ExpectedReport(conforms: Boolean, numErrors: Integer, profile: ProfileName, jsNumErrors: Option[Integer] = None)

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
      report     <- validation.validate(library, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  //what is speciy testing?? should be partitioned in a some new of tests?
  test("Trailing spaces validation") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "americanflightapi.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, RAMLProfile)
    } yield {
      assert(report.conforms)
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

  // is not a validation test, its cheking that the generated profile for effective validations exists
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

  // is not a validation test, its cheking that the generated profile for effective validations exists
  test("Can normalize a recursive array API") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(productionPath + "recursive2.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val A: ArrayShape = doc.asInstanceOf[Module].declares.head.asInstanceOf[ArrayShape]
      new ValidationResolutionPipeline(RAMLProfile, Module().withDeclares(Seq(A))).resolve()
      val profile = new AMFShapeValidations(A).profile(ObjectNode())
      assert(profile.violationLevel.size == 1)
      assert(
        profile.violationLevel.head == "file://amf-client/shared/src/test/resources/production/recursive2.raml#/declarations/types/array/A_validation")
    }
  }

  // is testing that the api has no errors. Should be in Platform?
  test("Some production api with includes") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(productionPath + "includes-api/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(library, RAMLProfile)
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
                         validation)
        .build()
      report <- validation.validate(doc, RAMLProfile)
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
                             validation)
        .build()
      report <- validation.validate(library, RAML08Profile)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  // this test should be refactored into 3 different examples test
  test("Test for different examples") {

    val validation = Validation(platform)
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/tck-examples/examples.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, RAMLProfile)
    } yield {
      assert(report.results.isEmpty)
    }
  }

  // this is a real case, recursion in json schema??
  test("Test stackoverflow case from Platform") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/stackoverflow/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, RAMLProfile)
    } yield {
      assert(!report.results.exists(_.validationId != ParserSideValidations.RecursiveShapeSpecification.id))
    }
  }

  // same than the previous one
  test("Test stackoverflow case 0.8 from Platform") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(validationsPath + "/stackoverflow2/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(library, RAML08Profile)
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
// why the generation???? Move to MovelValidationReportTest?
  test("Security scheme and traits test") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/securitySchemes/security1.raml", platform, RamlYamlHint, validation)
        .build()
      resolved <- Future {
        RAML10Plugin.resolve(doc)
      }
      generated <- new AMFSerializer(resolved, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps).renderToString
      report    <- validation.validate(doc, RAMLProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 2)
      assert(report.results.exists(_.message.contains("Security scheme 'undefined' not found in declarations.")))

    }
  }

  //test("Test resource type non string scalar parameter example") { its already tested in java parser tests

  test("Valid type example 1 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "validex1.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, AMFProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Valid type example 2 test") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "validex2.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, AMFProfile)
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
      report <- validation.validate(doc, AMFProfile)
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
      report <- validation.validate(doc, AMFProfile)
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
      report     <- validation.validate(doc, AMFProfile)
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
      report <- validation.validate(doc, RAMLProfile)
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
      report <- validation.validate(doc, RAMLProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("Test validate headers in request") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/parameters/request-header.json", platform, OasJsonHint, validation)
        .build()
      report <- validation.validate(doc, OASProfile, OASStyle)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test non existing include in type def") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/missing-includes/in-type-def.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, OASProfile, OASStyle)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test properties with special names") {
    for {
      validation <- Validation(platform)
      doc        <- AMFCompiler(validationsPath + "property-names.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(doc, AMFProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test enum number in string format validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "/enum-number-string/api.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test array without item type validation") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "production/array-without-items.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test validation with # in property shape name") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/invalid-char-property-name.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
    } yield {
      assert(report.conforms)
    }
  }

  test("baseUriParameters without baseUri") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "/no-base-uri.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAMLProfile, RAMLStyle)
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
      report <- validation.validate(doc, OASProfile, OASStyle)
    } yield {
      assert(report.conforms)
    }
  }

  test("Invalid security scheme") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "invalid-security.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAML08Profile)
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
      report <- validation.validate(doc, RAMLProfile)
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
      report <- validation.validate(doc, RAMLProfile)
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
      report <- validation.validate(doc, RAMLProfile)
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
      report <- validation.validate(doc, RAMLProfile)
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
      report <- validation.validate(doc, RAMLProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
      assert(report.results.head.message.equals("Expecting !!str and !!null provided"))
    }
  }

  test("HERE_HERE Exclusive Maximum Schema") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "08/max-exclusive-schema.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAML08Profile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Validate json schema with non url id.") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(productionPath + "card-data/currencyapi.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(doc, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("pattern raml example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model      <- AMFCompiler(validationsPath + "08/ramlpattern.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAMLProfile)
    } yield {
      assert(report.results.nonEmpty)
    }
  }

  test("lock-unlock example test") {
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationDialect()
      model <- AMFCompiler(productionPath + "lock-unlock/lockUnlockStats.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, RAMLProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
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
      report <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(model, RAML08Profile)
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
      report     <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(doc, RAML08Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("JSON Schema pattern properties") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "jsonSchemaProperties.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAMLProfile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
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
      report <- validation.validate(doc, RAML08Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Discriminator in union definition") {
    for {
      validation <- Validation(platform)
      doc <- AMFCompiler(validationsPath + "discriminator_union.raml", platform, RamlYamlHint, validation)
        .build()
      report   <- validation.validate(doc, RAML08Profile)
      resolved <- Future { RAML08Plugin.resolve(doc) }
    } yield {
      assert(!report.conforms)
    }
  }

  test("Date format not SYaml timestamp") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "types/mhra-e-payment-v1.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("JSON Schema Draft-3 required property support") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "jsonschema/misc_shapes.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAMLProfile)
    } yield {
      assert(!report.conforms)
    }
  }

  test("Connect and trace methods") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "connect-trace.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAML08Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Empty responses") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "empty-responses.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test recursive optional shape") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "recursive-optional-property.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, RAMLProfile)
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
      report <- validation.validate(model, RAMLProfile) // Change profile name here to validate for a different spec.
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
      report <- validation.validate(model, RAML08Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test valid recursive union recursive") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "shapes/union-recursive.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test more than one variable with link node in trait") {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(validationsPath + "traits/two-included-examples.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test different declarations with same name") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "declarations/api.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAML08Profile)
    } yield {
      assert(report.conforms)
    }
  }

  test("Test empty usage/uses entries") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(validationsPath + "empty-usage-uses.raml", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, RAMLProfile)
    } yield {
      assert(report.conforms)
    }
  }

  /*
  test("test field_nation") {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(productionPath + "field-nation-v2-api-2.0.7-fat-raml/FN_API_full.raml", platform, RamlYamlHint, validation)
        .build()
      report <- validation.validate(model, RAMLProfile)
    } yield {
      ExecutionLog.finish()
      ExecutionLog.buildReport()
      assert(report.conforms)
    }
  }
 */
}
