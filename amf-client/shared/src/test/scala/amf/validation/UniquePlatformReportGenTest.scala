package amf.validation

import _root_.org.scalatest.{Assertion, AsyncFunSuite}
import amf._
import amf.client.parse.DefaultParserErrorHandler
import amf.core.errorhandling.AmfReportBuilder
import amf.core.remote.Syntax.Yaml
import amf.core.remote._
import amf.core.validation.AMFValidationReport
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline

import scala.concurrent.{ExecutionContext, Future}

sealed trait ValidationReportGenTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
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
                         profileFile: Option[String] = None,
                         overridedHint: Option[Hint] = None): Future[Assertion] = {
    val eh        = DefaultParserErrorHandler.withRun()
    val finalHint = overridedHint.getOrElse(hint)
    for {
      validation <- Validation(platform)
      _ <- if (profileFile.isDefined)
        validation.loadValidationProfile(basePath + profileFile.get, DefaultParserErrorHandler.withRun())
      else Future.unit
      model  <- AMFCompiler(basePath + api, platform, finalHint, eh = eh).build()
      report <- validation.validate(model, profile)
      r      <- handleReport(report, golden.map(processGolden))
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
    val errorHandler = DefaultParserErrorHandler.withRun()
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(basePath + api, platform, profileToHint(profile), eh = errorHandler).build()
      report <- {
        new ValidationResolutionPipeline(profile, errorHandler).resolve(model)
        val results = errorHandler.getErrors
        val report  = new AmfReportBuilder(model, profile).buildReport(results)
        handleReport(report, golden)
      }
    } yield {
      report
    }
  }

  private def profileToHint(profile: ProfileName): Hint = {
    profile match {
      case OasProfile | Oas20Profile => OasJsonHint
      case Oas30Profile              => Hint(Oas30, Yaml)
      case _                         => RamlYamlHint
    }
  }
}

trait ValidModelTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = ""

  protected def checkValid(api: String, profile: ProfileName = RamlProfile): Future[Assertion] =
    super.validate(api, None, profile, None)

}
