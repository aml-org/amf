package amf.emit

import amf.apicontract.client.scala.{AsyncAPIConfiguration, RAMLConfiguration}
import amf.apicontract.client.scala.{AMFConfiguration, OASConfiguration}
import amf.core.client.common.validation._
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.common.transform._
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Syntax.Syntax
import amf.core.internal.remote._
import amf.core.internal.unsafe.PlatformSecrets
import amf.io.FunSuiteCycleTests
import amf.testing.TargetProvider.defaultTargetFor
import org.mulesoft.common.io.AsyncFile
import org.scalatest.Matchers

import scala.concurrent.Future
import scala.concurrent.Future.successful

class CompatibilityCycledValidationTest extends CompatibilityCycle {

  override val basePath = "amf-cli/shared/src/test/resources/compatibility/"

  testCycleCompatibility("oas30", Oas30JsonHint, Raml10, basePath)
  testCycleCompatibility("oas20", Oas20JsonHint, Raml10, basePath)
  testCycleCompatibility("raml10", Raml10YamlHint, Oas30, basePath)
  testCycleCompatibility("raml10", Raml10YamlHint, Oas20, basePath)
}

trait CompatibilityCycle extends FunSuiteCycleTests with Matchers with PlatformSecrets {

  private val REPORT_CONFORMS = "Conforms? true"

  def testCycleCompatibility(filePath: String,
                             from: Hint,
                             to: Vendor,
                             basePath: String,
                             syntax: Option[Syntax] = None,
                             pipeline: Option[String] = None): Unit = {
    for {
      file <- platform.fs.syncFile(basePath + filePath).list.sorted
    } {
      val path = s"$filePath/$file"

      test(s"Test $path to $to") {
        val config       = CycleConfig(path, path, from, defaultTargetFor(to), basePath, syntax, pipeline)
        val targetHint   = hint(vendor = to)
        val toProfile    = profile(to)
        val amfConfig    = buildConfig(None, None)
        val targetConfig = buildConfig(amfConfigFrom(to), None, None)
        for {
          origin   <- build(config, amfConfig)
          resolved <- successful(transform(origin, config, targetConfig))
          rendered <- successful(render(resolved, config, targetConfig))
          tmp      <- writeTemporaryFile(path)(rendered)
          report   <- validate(tmp, toProfile)
        } yield {
          outputReportErrors(report)
        }
      }
    }
  }

  private def amfConfigFrom(vendor: Vendor): AMFConfiguration = vendor match {
    case Vendor.OAS30   => OASConfiguration.OAS30()
    case Vendor.OAS20   => OASConfiguration.OAS20()
    case Vendor.RAML10  => RAMLConfiguration.RAML10()
    case Vendor.RAML08  => RAMLConfiguration.RAML08()
    case Vendor.ASYNC20 => AsyncAPIConfiguration.Async20()
    case _              => throw new IllegalArgumentException
  }

  private def hint(vendor: Vendor) = vendor match {
    case Raml10 => Raml10YamlHint
    case Raml08 => Raml08YamlHint
    case Oas20  => Oas20YamlHint
    case Oas30  => Oas30YamlHint
    case _      => throw new IllegalArgumentException
  }

  private def outputReportErrors(report: AMFValidationReport) = report.toString should include(REPORT_CONFORMS)

  private def validate(source: AsyncFile, profileName: ProfileName): Future[AMFValidationReport] = {
    val handler   = DefaultErrorHandler()
    val amfConfig = buildConfig(None, Some(handler))
    build(source.path, source.path, amfConfig).flatMap { unit =>
      amfConfig.baseUnitClient().validate(unit, profileName)
    }
  }

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    amfConfig
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .baseUnitClient()
      .transform(unit, PipelineId.Compatibility)
      .baseUnit
  }

  private def profile(vendor: Vendor): ProfileName = vendor match {
    case Raml10 => Raml10Profile
    case Raml08 => Raml08Profile
    case Oas20  => Oas20Profile
    case Oas30  => Oas30Profile
    case _      => throw new IllegalArgumentException
  }
}
