package amf.emit

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.validation.AMFValidationReport
import amf.facades.Validation
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.compatibility.RamlCompatibilityPipeline
import org.mulesoft.common.io.AsyncFile
import org.scalatest.Matchers

import scala.concurrent.Future

class CompatibilityCycleTest extends FunSuiteCycleTests with Matchers {

  override val basePath = "amf-client/shared/src/test/resources/compatibility/"

  for {
    file <- platform.fs.syncFile(basePath + "oas20").list
  } {
    // For each oas -> render raml and validate
    val path = s"oas20/$file"

    test(s"Test $path") {
      val c = CycleConfig(path, path, OasJsonHint, Raml, basePath, None)
      println(s"About to convert $path")
      for {
        origin   <- build(c, None, useAmfJsonldSerialisation = true)
        rendered <- render(origin, c, useAmfJsonldSerialization = true)
        tmp      <- writeTemporaryFile(path)(rendered)
        report   <- validate(tmp, RamlYamlHint)
      } yield {
        report.toString should include("Conforms? true")
      }
    }
  }

  private def validate(source: AsyncFile, hint: Hint): Future[AMFValidationReport] = {
    println(s"About to validate ${source.path}")
    Validation(platform)
      .map(_.withEnabledValidation(false))
      .flatMap(validation => {
        val config = CycleConfig(source.path, source.path, hint, hint.vendor, "", None)
        build(config, Some(validation), useAmfJsonldSerialisation = true).flatMap(unit => {
          validation.validate(unit, ProfileNames.RAML)
        })
      })
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml | Raml08 | Raml10 => RamlCompatibilityPipeline.unhandled.resolve(unit)
    case Oas | Oas20 | Oas30    => ???
  }
}
