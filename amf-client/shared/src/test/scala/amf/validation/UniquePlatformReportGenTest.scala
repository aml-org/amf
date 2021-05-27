package amf.validation

import _root_.org.scalatest.{Assertion, AsyncFunSuite}
import amf._
import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.parse.DefaultErrorHandler
import amf.client.remod.{AMFGraphConfiguration, AMFResult}
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.errorhandling.{AMFErrorHandler, AmfReportBuilder}
import amf.core.model.document.BaseUnit
import amf.core.remote.Syntax.Yaml
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipelineRunner
import amf.core.validation.AMFValidationReport
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.resolution.pipelines.ValidationTransformationPipeline

import scala.concurrent.{ExecutionContext, Future}

sealed trait AMFValidationReportGenTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val basePath: String
  val reportsPath: String
  val hint: Hint

  protected lazy val defaultProfile: ProfileName = hint.vendor match {
    case Raml10 => Raml10Profile
    case Raml08 => Raml08Profile
    case Oas20  => Oas20Profile
    case Oas30  => Oas30Profile
    case _      => AmfProfile
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
                         overridedHint: Option[Hint] = None,
                         directory: String = basePath): Future[Assertion] = {
    val eh        = DefaultErrorHandler()
    val finalHint = overridedHint.getOrElse(hint)
    for {
      validation <- Validation(platform)
      _ <- if (profileFile.isDefined)
        validation.loadValidationProfile(directory + profileFile.get, DefaultErrorHandler())
      else Future.unit
      parseResult <- parse(directory + api, eh, finalHint)
      report <- validation.validate(parseResult.bu,
                                    profile,
                                    new ValidationConfiguration(AMFGraphConfiguration.fromEH(eh)))
      r <- {
        val finalReport =
          if (!parseResult.conforms) parseResult.result
          else parseResult.result.merge(report)
        handleReport(finalReport, golden.map(processGolden))
      }
    } yield {
      r
    }
  }

  protected def parse(path: String, eh: AMFErrorHandler, finalHint: Hint): Future[AMFResult] = {
    val client = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).createClient()
    client.parse(path)
  }

  protected def processGolden(g: String): String
}

trait UniquePlatformReportGenTest extends AMFValidationReportGenTest {
  override protected def processGolden(g: String): String = g
}

trait MultiPlatformReportGenTest extends AMFValidationReportGenTest {
  override protected def processGolden(g: String): String = g + s".${platform.name}"
}

trait ResolutionForUniquePlatformReportTest extends UniquePlatformReportGenTest {

  protected def checkReport(api: String,
                            golden: Option[String] = None,
                            profile: ProfileName = defaultProfile,
                            profileFile: Option[String] = None): Future[Assertion] = {
    val errorHandler = DefaultErrorHandler()
    val config       = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => errorHandler)
    for {
      validation <- Validation(platform)
      model      <- config.createClient().parse(basePath + api).map(_.bu)
      report <- {
        TransformationPipelineRunner(errorHandler).run(model, new ValidationTransformationPipeline(profile))
        val results = errorHandler.getResults
        val report  = new AmfReportBuilder(model, profile).buildReport(results)
        handleReport(report, golden)
      }
    } yield {
      report
    }
  }

  private def profileToHint(profile: ProfileName): Hint = {
    profile match {
      case Oas20Profile => Oas20JsonHint
      case Oas30Profile => Hint(Oas30, Yaml)
      case _            => Raml10YamlHint
    }
  }
}

trait ValidModelTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = ""

  protected def checkValid(api: String, profile: ProfileName = Raml10Profile): Future[Assertion] =
    super.validate(api, None, profile, None)

}
