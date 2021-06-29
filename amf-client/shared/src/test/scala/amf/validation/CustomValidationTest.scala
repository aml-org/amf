package amf.validation
import amf.client.parse.DefaultParserErrorHandler
import amf.core.remote.{Hint, RamlYamlHint}
import amf.facades.Validation
import amf.{ProfileName, RAMLStyle}
import org.scalatest.Assertion

import scala.concurrent.Future

class CustomValidationTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/"
  override val hint: Hint  = RamlYamlHint

  test("Load dialect") {
    validate("mercury.raml",
             Some(s"mercury1.report.${platform.name}"),
             ProfileName("mercury"),
             Some("profiles/mercury.yaml"))
  }

  test("Additional raml validations") {
    validate("amf_extended.raml",
             Some("amf_extended.report"),
             ProfileName("amf_extended", RAMLStyle),
             Some("profiles/amf_extended.yaml"))
  }

  test("Property pairs validations") {
    validate("pairs.raml", Some("pairs.report"), ProfileName("pairs"), Some("profiles/pairs.yaml"))
  }

  test("Paths validations") {
    validate("paths.raml", Some("paths.report"), ProfileName("paths"), Some("profiles/paths.yaml"))
  }

  test("Paths validations 2") {
    validate("paths2.raml", Some("paths2.report"), ProfileName("paths2"), Some("profiles/paths2.yaml"))
  }

  test("Logical constraints") {
    validate("logical.raml", Some("logical.report"), ProfileName("logical"), Some("profiles/logical.yaml"))
  }

  test("Defining raml validation as warning") {
    validate("banking-api-error.raml",
             Some("banking.report"),
             ProfileName("Banking"),
             Some("profiles/validation-profile.yaml"))
  }

  test("Previous test but loading profile from string") {
    val profile = readFileContent("profiles/validation-profile.yaml")
    validateWithStringProfile("banking-api-error.raml", Some("banking.report"), ProfileName("Banking"), profile)
  }

  if (platform.name == "jvm") { // not supported in JS yet
    test("Query constraints") {
      validate("query.raml", Some("query.report"), ProfileName("query"), Some("profiles/query.yaml"))
    }
  }

  private def validateWithStringProfile(api: String,
                                        golden: Option[String] = None,
                                        profile: ProfileName,
                                        profileContent: String,
                                        directory: String = basePath): Future[Assertion] = {
    val eh = DefaultParserErrorHandler.withRun()
    for {
      validation <- Validation(platform)
      _          <- validation.loadValidationProfileString(profileContent, DefaultParserErrorHandler.withRun())
      model      <- parse(directory + api, eh, hint)
      report     <- validation.validate(model, profile)
      r          <- handleReport(report, golden.map(processGolden))
    } yield {
      r
    }
  }

  private def readFileContent(path: String): String = {
    val file = platform.fs.syncFile(basePath.stripPrefix("file://") + path)
    file.read().toString
  }
}
