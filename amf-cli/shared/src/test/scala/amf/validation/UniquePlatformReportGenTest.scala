package amf.validation

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.apicontract.internal.transformation.ValidationTransformationPipeline
import amf.core.client.common.validation._
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote._
import amf.core.io.FileAssertionTest
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion

import scala.concurrent.Future

sealed trait AMFValidationReportGenTest extends FileAssertionTest {

  val basePath: String
  val reportsPath: String

  protected def generate(report: AMFValidationReport): String = {
    report.toString
  }

  protected def handleReport(
      report: AMFValidationReport,
      golden: Option[String],
      ignoreWarnings: Boolean
  ): Future[Assertion] =
    golden match {
      case Some(_) =>
        writeTemporaryFile(golden.get)(generate(report)).flatMap(assertDifferences(_, reportsPath + golden.get))
      case None =>
        Future.successful({
          if (!report.conforms) fail("Report not conforms:\n" + report.toString)
          if (report.results.nonEmpty && !ignoreWarnings)
            fail("Report conforms but there is results, probably some warnings\n:" + report.toString)
          succeed
        })
      // i have to check results in order to check warnings. If you pass none, and you are expeting some warnings, you should use the file assertion to check the warnings messages.
    }

  protected def validate(
      api: String,
      golden: Option[String] = None,
      directory: String = basePath,
      configOverride: Option[AMFConfiguration] = None,
      hideValidationResultsIfParseNotConforms: Boolean = true,
      ignoreWarnings: Boolean = false
  ): Future[Assertion] = {
    val initialConfig = configOverride.getOrElse(APIConfiguration.API())
    for {
      parseResult <- parse(directory + api, initialConfig)
      report <- configOverride
        .getOrElse(configFor(parseResult.sourceSpec))
        .baseUnitClient()
        .validate(parseResult.baseUnit)
      r <- {
        val parseReport = AMFValidationReport.unknownProfile(parseResult)
        val finalReport =
          if (!parseResult.conforms && hideValidationResultsIfParseNotConforms) parseReport
          else parseReport.merge(report)
        handleReport(finalReport, golden.map(processGolden), ignoreWarnings)
      }
    } yield {
      r
    }
  }

  protected def parse(path: String, conf: AMFConfiguration) = {
    val client = conf.baseUnitClient()
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

  val hint: Hint

  protected def checkReport(
      api: String,
      golden: Option[String] = None,
      profile: ProfileName = defaultProfile,
      ignoreWarnings: Boolean = false
  ): Future[Assertion] = {
    val errorHandler = DefaultErrorHandler()
    val config = APIConfiguration
      .API()
      .withErrorHandlerProvider(() => errorHandler)
    for {
      model <- config.baseUnitClient().parse(basePath + api).map(_.baseUnit)
      report <- {
        TransformationPipelineRunner(errorHandler, config).run(model, new ValidationTransformationPipeline(profile))
        val results = errorHandler.getResults
        val report  = AMFValidationReport(model.id, profile, results)
        handleReport(report, golden, ignoreWarnings)
      }
    } yield {
      report
    }
  }

  protected def defaultProfile: ProfileName = hint.spec match {
    case Raml10     => Raml10Profile
    case Raml08     => Raml08Profile
    case Oas20      => Oas20Profile
    case Oas30      => Oas30Profile
    case Oas31      => Oas31Profile
    case AsyncApi20 => Async20Profile
    case _          => AmfProfile
  }
}

trait ValidModelTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath: String = "file://amf-cli/shared/src/test/resources/validations/reports/"

  protected def checkValid(
      api: String,
      profile: ProfileName = Raml10Profile,
      configOverride: Option[AMFConfiguration] = None
  ): Future[Assertion] =
    super.validate(api, configOverride = configOverride)

}
