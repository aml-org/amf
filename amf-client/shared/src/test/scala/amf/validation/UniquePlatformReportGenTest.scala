package amf.validation

import amf._
import amf.core.remote.{Hint, Oas, Raml, RamlYamlHint}
import amf.core.validation.{AMFValidationReport, SeverityLevels}
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import _root_.org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.Future

sealed trait ValidationReportGenTest extends AsyncFunSuite with FileAssertionTest {

  val basePath: String
  val reportsPath: String
  val hint: Hint

  protected lazy val defaultProfile: ProfileName = hint.vendor match {
    case Raml => Raml10Profile
    case Oas  => Oas20Profile
    case _    => AmfProfile
  }

  protected def generate(report: AMFValidationReport): String = {
    report.toString
  }

  protected def handleReport(report: AMFValidationReport, golden: Option[String]): Future[Assertion] =
    golden match {
      case Some(g) =>
        writeTemporaryFile(golden.get)(generate(report)).flatMap(assertDifferences(_, reportsPath + golden.get))
      case None =>
        Future.successful({
          if (!report.conforms) fail("Report not conforms:\n" + report.toString)
          if (report.results.nonEmpty)
            fail("Report conforms but there is results, probably some warnings\n:" + report.toString)
          succeed
        })
      // i have to check results in order to check warnings. If you pass none, and you are expeting some warnings, you should use the file assertion to check the warnings messages.
    }

  protected def validate(api: String,
                         golden: Option[String] = None,
                         profile: ProfileName = defaultProfile,
                         profileFile: Option[String] = None): Future[Assertion] = {
    for {
      validation <- Validation(platform)
      _          <- if (profileFile.isDefined) validation.loadValidationProfile(basePath + profileFile.get) else Future.unit
      model      <- AMFCompiler(basePath + api, platform, hint, validation).build()
      report     <- validation.validate(model, profile)
      r          <- handleReport(report, golden.map(processGolden))
    } yield {
      r
    }
  }

  protected def processGolden(g: String): String
}

trait UniquePlatformReportGenTest extends ValidationReportGenTest {
  override protected def processGolden(g: String): String = g
}

trait MultiPlatformReportGenTest extends ValidationReportGenTest {
  override protected def processGolden(g: String): String = g + s".${platform.name}"
}

trait ResolutionForUniquePlatformReportTest extends UniquePlatformReportGenTest {

  protected def checkReport(api: String,
                            golden: Option[String] = None,
                            profile: ProfileName = defaultProfile,
                            profileFile: Option[String] = None): Future[Assertion] = {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(basePath + api, platform, RamlYamlHint, validation).build()
      report <- {
        new ValidationResolutionPipeline(profile, model).resolve()
        val results = validation.aggregatedReport
        val report =
          AMFValidationReport(!results.exists(_.level == SeverityLevels.VIOLATION), model.id, profile, results)
        handleReport(report, golden)
      }
    } yield {
      report
    }
  }
}

trait ValidModelTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = ""

  protected def checkValid(api: String, profile: ProfileName = RamlProfile): Future[Assertion] =
    super.validate(api, None, profile, None)

}
