package amf.validation

import amf.ProfileNames
import amf.core.emitter.RenderOptions
import amf.core.model.document.{Module, PayloadFragment}
import amf.core.model.domain.Shape
import amf.core.remote.{PayloadJsonHint, PayloadYamlHint, RamlYamlHint}
import amf.core.unsafe.{PlatformSecrets, TrunkPlatform}
import amf.core.validation.ValidationCandidate
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.graph.parser.GraphEmitter
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.plugins.document.webapi.validation.PayloadValidation
import org.scalatest.AsyncFunSuite
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

class GenericPayloadValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val payloadsPath = "file://parser-client/shared/src/test/resources/payloads/"

  val payloadValidations = Map(
    ("payloads.raml", "A", "a_valid.json")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "A", "a_invalid.json")                      -> ExpectedReport(conforms = false, 4, "Payload"),
    ("payloads.raml", "B", "b_valid.json")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "B", "b_invalid.json")                      -> ExpectedReport(conforms = false, 1, "Payload"),
    ("payloads.raml", "B", "b_valid.yaml")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "B", "b_invalid.yaml")                      -> ExpectedReport(conforms = false, 1, "Payload"),
    ("payloads.raml", "C", "c_valid.json")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "C", "c_invalid.json")                      -> ExpectedReport(conforms = false, 8, "Payload"),
    ("payloads.raml", "D", "d_valid.json")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "D", "d_invalid.json")                      -> ExpectedReport(conforms = false, 7, "Payload"),
    ("payloads.raml", "E", "e_valid.json")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "E", "e_invalid.json")                      -> ExpectedReport(conforms = false, 1, "Payload"),
    ("payloads.raml", "F", "f_valid.json")                        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "F", "f_invalid.json")                      -> ExpectedReport(conforms = false, 1, "Payload"),
    ("payloads.raml", "G", "g1_valid.json")                       -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "G", "g2_valid.json")                       -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "G", "g_invalid.json")                      -> ExpectedReport(conforms = false, 1, "Payload"),
    ("payloads.raml", "H", "h_invalid.json")                      -> ExpectedReport(conforms = false, 1, "Payload"),
    ("payloads.raml", "PersonData", "person_valid.yaml")          -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "PersonData", "person_invalid.yaml")        -> ExpectedReport(conforms = false, 2, "Payload"),
    ("payloads.raml", "CustomerData", "customer_data_valid.yaml") -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "CustomerData", "person_valid.yaml")        -> ExpectedReport(conforms = true, 0, "Payload"),
    ("payloads.raml", "CustomerData", "person_invalid.yaml")      -> ExpectedReport(conforms = false, 2, "Payload"),
    ("test_cases.raml", "A", "test_case_a_valid.json")            -> ExpectedReport(conforms = true, 0, "Payload"),
    ("test_cases.raml", "A", "test_case_a_invalid.json")          -> ExpectedReport(conforms = false, 2, "Payload"),
    ("test_cases.raml", "A", "test_case_a2_valid.json")           -> ExpectedReport(conforms = true, 0, "Payload"),
    ("test_cases.raml", "A", "test_case_a2_invalid.json")         -> ExpectedReport(conforms = false, 2, "Payload")
  )

  for {
    ((libraryFile, shapeName, payloadFile), expectedReport) <- payloadValidations
  } yield {
    test(s"SHACL Payload Validator $shapeName -> $payloadFile") {
      val hint = payloadFile.split("\\.").last match {
        case "json" => PayloadJsonHint
        case "yaml" => PayloadYamlHint
      }
      val validation: Future[PayloadValidation] = for {
        validation <- Validation(platform).map(_.withEnabledValidation(false))
        library    <- AMFCompiler(payloadsPath + libraryFile, platform, RamlYamlHint, validation).build()
        payload    <- AMFCompiler(payloadsPath + payloadFile, platform, hint, validation).build()
      } yield {
        // todo check with antonio, i removed the canonical shape from validation, so i need to resolve here
        new ValidationResolutionPipeline(ProfileNames.AMF).resolve(library)
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
}
