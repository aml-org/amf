package amf.validation
import amf.core.model.document.{BaseUnit, Module, PayloadFragment}
import amf.core.model.domain.Shape
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.{PayloadJsonHint, PayloadYamlHint, RamlYamlHint}
import amf.core.unsafe.{PlatformSecrets, TrunkPlatform}
import amf.core.validation.{SeverityLevels, ValidationCandidate}
import amf.facades.{AMFCompiler, Validation}
import amf.internal.environment.Environment
import amf.plugins.document.graph.emitter.JsonLdEmitter
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.plugins.domain.shapes.validation.PayloadValidationPluginsHandler
import amf.{AmfProfile, PayloadProfile}
import org.scalatest.AsyncFunSuite
import org.yaml.builder.JsonOutputBuilder

import scala.concurrent.{ExecutionContext, Future}

class GenericPayloadValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val payloadsPath = "file://amf-client/shared/src/test/resources/payloads/"

  val payloadValidations = Map(
    ("payloads.raml", "A", "a_valid.json")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "A", "a_invalid.json") -> ExpectedReport(conforms = false, 2, PayloadProfile),
    ("payloads.raml", "B", "b_valid.json")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "B", "b_invalid.json") -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("payloads.raml", "B", "b_valid.yaml")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "B", "b_invalid.yaml") -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("payloads.raml", "C", "c_valid.json")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "C", "c_invalid.json") -> ExpectedReport(conforms = false, 4, PayloadProfile),
    ("payloads.raml", "D", "d_valid.json")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    // jvm reports the failures in the inner node and the failed value for the property connecting the inner node,
    // js only reports the failed properties in the inner node
    ("payloads.raml", "D", "d_invalid.json") -> ExpectedReport(conforms = false, 2, PayloadProfile),
    ("payloads.raml", "E", "e_valid.json")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "E", "e_invalid.json") -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("payloads.raml", "F", "f_valid.json")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "F", "f_invalid.json") -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("payloads.raml", "G", "g1_valid.json")  -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "G", "g2_valid.json")  -> ExpectedReport(conforms = true, 0, PayloadProfile),
    // jvm reports two nested error for the anyOf
    // js reports an error for each failed shape and one more for the fialed anyOf
    ("payloads.raml", "G", "g_invalid.json") -> ExpectedReport(conforms = false,
                                                               2,
                                                               PayloadProfile,
                                                               jsNumErrors = Some(3)),
    ("payloads.raml", "H", "h_invalid.json")               -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("payloads.raml", "PersonData", "person_valid.yaml")   -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("payloads.raml", "PersonData", "person_invalid.yaml") -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("payloads.raml", "CustomerData", "customer_data_valid.yaml") -> ExpectedReport(conforms = true,
                                                                                    0,
                                                                                    PayloadProfile),
    ("payloads.raml", "CustomerData", "person_valid.yaml") -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("test_cases.raml", "A", "test_case_a_valid.json")     -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("test_cases.raml", "A", "test_case_a_invalid.json")   -> ExpectedReport(conforms = false, 1, PayloadProfile),
    ("test_cases.raml", "A", "test_case_a2_valid.json")    -> ExpectedReport(conforms = true, 0, PayloadProfile),
    ("test_cases.raml", "A", "test_case_a2_invalid.json")  -> ExpectedReport(conforms = false, 1, PayloadProfile)
  )

  for {
    ((libraryFile, shapeName, payloadFile), expectedReport) <- payloadValidations
  } yield {
    test(s"Payload Validator $shapeName -> $payloadFile") {
      val hint = payloadFile.split("\\.").last match {
        case "json" => PayloadJsonHint
        case "yaml" => PayloadYamlHint
      }
      val candidates: Future[Seq[ValidationCandidate]] = for {
        validation <- Validation(platform)
        library <- AMFCompiler(payloadsPath + libraryFile, platform, RamlYamlHint, eh = UnhandledParserErrorHandler)
          .build()
        payload <- AMFCompiler(payloadsPath + payloadFile, platform, hint, eh = UnhandledParserErrorHandler).build()
      } yield {
        // todo check with antonio, i removed the canonical shape from validation, so i need to resolve here
        ValidationResolutionPipeline(AmfProfile, library)
        val targetType = library
          .asInstanceOf[Module]
          .declares
          .find {
            case s: Shape => s.name.is(shapeName)
          }
          .get

        Seq(ValidationCandidate(targetType.asInstanceOf[Shape], payload.asInstanceOf[PayloadFragment]))
      }

      candidates flatMap { c =>
        PayloadValidationPluginsHandler.validateAll(c, SeverityLevels.VIOLATION, Environment())
      } map { report =>
        report.results.foreach { result =>
          assert(result.position.isDefined)
        }
        assert(report.conforms == expectedReport.conforms)
        if (expectedReport.jsNumErrors.isDefined && platform.name == "js") {
          assert(report.results.length == expectedReport.jsNumErrors.get)
        } else {
          assert(report.results.length == expectedReport.numErrors)
        }
      }
    }
  }

  test("payload parsing test") {

    for {
      content    <- platform.resolve(payloadsPath + "b_valid.yaml")
      validation <- Validation(platform)
      filePayload <- AMFCompiler(payloadsPath + "b_valid.yaml",
                                 platform,
                                 PayloadYamlHint,
                                 eh = UnhandledParserErrorHandler)
        .build()
      validationPayload <- Validation(platform)
      textPayload <- AMFCompiler(
        payloadsPath + "b_valid.yaml",
        TrunkPlatform(content.stream.toString, forcedMediaType = Some("application/yaml")),
        PayloadYamlHint,
        eh = UnhandledParserErrorHandler
      ).build()
    } yield {
      val fileJson = render(filePayload)
      val textJson = render(textPayload)
      assert(fileJson == textJson)
    }

  }
  private def render(filePayload: BaseUnit) = {
    val builder = JsonOutputBuilder()
    JsonLdEmitter.emit(filePayload, builder)
    builder.result.toString
  }
}
