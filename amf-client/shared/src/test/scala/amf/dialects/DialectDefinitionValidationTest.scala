package amf.dialects
import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

trait DialectDefinitionValidationTest extends AsyncFunSuite with Matchers with ReportComparison with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test missing version") {
    validate("/missing-version/dialect.yaml", Some("/missing-version/report.json"))
  }

  test("Test missing dialect name") {
    validate("/missing-dialect-name/dialect.yaml", Some("/missing-dialect-name/report.json"))
  }

  test("Test invalid property term uri for description") {
    validate("/schema-uri/dialect.yaml", Some("/schema-uri/report.json"))
  }

  test("Test missing range in property mapping") {
    validate("/missing-range-in-mapping/dialect.yaml", Some("/missing-range-in-mapping/report.json"))
  }

  private val path: String = "amf-client/shared/src/test/resources/vocabularies2/instances/invalids"

  protected def validate(dialect: String, goldenReport: Option[String]): Future[Assertion] = {
    amf.core.AMF.registerPlugin(AMLPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    for {
      _ <- Validation(platform)
      dialect <- {
        new AMFCompiler(
          new CompilerContextBuilder("file://" + path + dialect, platform, eh = DefaultParserErrorHandler.withRun())
            .build(),
          Some("application/yaml"),
          Some(AMLPlugin.ID)
        ).build()
      }
      report <- {
        RuntimeValidator(
          dialect,
          ProfileName(dialect.asInstanceOf[Dialect].nameAndVersion())
        )
      }
      assertion <- assertReport(report, goldenReport.map(g => s"$path/$g"))
    } yield {
      assertion
    }
  }
}
