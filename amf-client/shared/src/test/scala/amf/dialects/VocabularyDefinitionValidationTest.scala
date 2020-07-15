package amf.dialects
import amf.AmlProfile
import amf.client.parse.DefaultParserErrorHandler
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationReport
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.io.FileAssertionTest
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter
import org.scalatest
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class VocabularyDefinitionValidationTest
    extends AsyncFunSuite
    with Matchers
    with FileAssertionTest
    with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/validations/vocabularies"

  test("Test repeated term in property terms and class terms") {
    validate("vocabulary.yaml", Some("validation.jsonld"), "repeated-term")
  }

  private def compilerContext(url: String) =
    new CompilerContextBuilder(url, platform, eh = DefaultParserErrorHandler.withRun()).build()

  protected def validate(vocabulary: String,
                         goldenReport: Option[String] = None,
                         path: String): Future[scalatest.Assertion] = {
    amf.core.AMF.registerPlugin(plugin = AMLPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    val vocabularyContext = compilerContext(s"file://$basePath/$path/$vocabulary")
    val report = for {
      _ <- amf.core.AMF.init()
      vocabulary <- {
        new AMFCompiler(
          vocabularyContext,
          Some("application/yaml"),
          None
        ).build()
      }
      report <- RuntimeValidator(vocabulary, AmlProfile)
    } yield {
      report
    }

    report.flatMap { re =>
      goldenReport match {
        case Some(golden) =>
          writeTemporaryFile(s"$basePath/$path/$golden")(ValidationReportJSONLDEmitter.emitJSON(re))
            .flatMap(assertDifferences(_, s"$basePath/$path/$golden"))
        case None => re.conforms should be(true)
      }
    }
  }

}
