package amf.validation
import amf.apicontract.client.scala.RAMLConfiguration
import amf.apicontract.internal.transformation.ValidationTransformationPipeline
import amf.apicontract.internal.validation.payload.CandidateValidator
import amf.core.client.common.validation.{AmfProfile, PayloadProfile, SeverityLevels}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Module, PayloadFragment}
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.document.graph.emitter.EmbeddedJsonLdEmitter
import amf.core.internal.plugins.payload.ErrorFallbackValidationPlugin
import amf.core.internal.remote.{PayloadJsonHint, PayloadYamlHint}
import amf.core.internal.unsafe.{PlatformSecrets, TrunkPlatform}
import amf.core.internal.validation.{ValidationCandidate, ValidationConfiguration}
import org.scalatest.AsyncFunSuite
import org.yaml.builder.JsonOutputBuilder

import scala.concurrent.{ExecutionContext, Future}

class GenericPayloadValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val payloadsPath = "file://amf-cli/shared/src/test/resources/payloads/"

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
      val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
      val client = config.baseUnitClient()
      val candidates: Future[Seq[ValidationCandidate]] = for {
        library <- client.parse(payloadsPath + libraryFile).map(_.baseUnit)
        payload <- client.parse(payloadsPath + payloadFile, hint.vendor.mediaType).map(_.baseUnit)
      } yield {
        // todo check with antonio, i removed the canonical shape from validation, so i need to resolve here
        ValidationTransformationPipeline(AmfProfile, library, UnhandledErrorHandler)
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
        val nextConfig = config.withPlugin(ErrorFallbackValidationPlugin(SeverityLevels.VIOLATION))
        CandidateValidator.validateAll(c, new ValidationConfiguration(nextConfig))
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

//  test("payload parsing test") {
//    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
//    for {
//      content    <- platform.fetchContent(payloadsPath + "b_valid.yaml", config)
//      filePayload <- AMFCompiler(payloadsPath + "b_valid.yaml", platform, PayloadYamlHint, config = config)
//        .build()
//      validationPayload <- Validation(platform)
//      textPayload <- AMFCompiler(
//        payloadsPath + "b_valid.yaml",
//        TrunkPlatform(content.stream.toString, forcedMediaType = Some("application/yaml")),
//        PayloadYamlHint,
//        config = config
//      ).build()
//    } yield {
//      val fileJson = render(filePayload)
//      val textJson = render(textPayload)
//      assert(fileJson == textJson)
//    }
//
//  }
  private def render(filePayload: BaseUnit) = {
    val builder = JsonOutputBuilder()
    EmbeddedJsonLdEmitter.emit(filePayload, builder)
    builder.result.toString
  }
}
