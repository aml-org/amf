package amf.emit

import amf.client.parse.DefaultParserErrorHandler
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Syntax.Syntax
import amf.core.remote._
import amf.core.resolution.pipelines.{TransformationPipeline, TransformationPipelineRunner}
import amf.core.validation.AMFValidationReport
import amf.facades.Validation
import amf.io.FunSuiteCycleTests
import amf._
import org.mulesoft.common.io.AsyncFile
import org.scalatest.Matchers

import scala.concurrent.Future
import scala.concurrent.Future.successful

class CompatibilityCycledValidationTest extends CompatibilityCycle {

  override val basePath = "amf-client/shared/src/test/resources/compatibility/"

  testCycleCompatibility("oas30", Oas30JsonHint, Raml10, basePath)
  testCycleCompatibility("oas20", Oas20JsonHint, Raml10, basePath)
  testCycleCompatibility("raml10", Raml10YamlHint, Oas30, basePath)
  testCycleCompatibility("raml10", Raml10YamlHint, Oas20, basePath)
}

trait CompatibilityCycle extends FunSuiteCycleTests with Matchers {

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
        val config     = CycleConfig(path, path, from, to, basePath, syntax, pipeline)
        val targetHint = hint(vendor = to)
        val toProfile  = profile(to)
        for {
          origin   <- build(config, Some(DefaultParserErrorHandler.withRun()), useAmfJsonldSerialisation = true)
          resolved <- successful(transform(origin, config))
          rendered <- render(resolved, config, useAmfJsonldSerialization = true)
          tmp      <- writeTemporaryFile(path)(rendered)
          report   <- validate(tmp, targetHint, toProfile, to)
        } yield {
          outputReportErrors(report)
        }
      }
    }
  }

  private def hint(vendor: Vendor) = vendor match {
    case Raml10 => Raml10YamlHint
    case Raml08 => Raml08YamlHint
    case Oas20  => Oas20YamlHint
    case Oas30  => Oas30YamlHint
    case _      => throw new IllegalArgumentException
  }

  private def outputReportErrors(report: AMFValidationReport) = report.toString should include(REPORT_CONFORMS)

  private def validate(source: AsyncFile,
                       hint: Hint,
                       profileName: ProfileName,
                       target: Vendor): Future[AMFValidationReport] =
    Validation(platform)
      .flatMap { validation =>
        val config = CycleConfig(source.path, source.path, hint, target, "", None, None)
        build(config, Some(DefaultParserErrorHandler.withRun()), useAmfJsonldSerialisation = true).flatMap { unit =>
          validation.validate(unit, profileName)
        }
      }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    // TODO: ARM change for AMFTransformer.transform
    val pipeline = AMFPluginsRegistry.staticCofiguration.registry
      .transformationPipelines(PipelineName.from(config.target.name, TransformationPipeline.COMPATIBILITY_PIPELINE))
    TransformationPipelineRunner(UnhandledErrorHandler).run(unit, pipeline)
  }

  private def profile(vendor: Vendor): ProfileName = vendor match {
    case Raml10 => Raml10Profile
    case Raml08 => Raml08Profile
    case Oas20  => Oas20Profile
    case Oas30  => Oas30Profile
    case _      => throw new IllegalArgumentException
  }
}
