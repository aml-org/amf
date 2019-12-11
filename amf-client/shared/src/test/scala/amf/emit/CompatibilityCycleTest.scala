package amf.emit

import amf.{OasProfile, ProfileNames}
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.validation.AMFValidationReport
import amf.facades.Validation
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.compatibility.CompatibilityPipeline
import org.mulesoft.common.io.AsyncFile
import org.scalatest.Matchers

import scala.concurrent.Future
import scala.concurrent.Future.successful

class CompatibilityCycleTest extends FunSuiteCycleTests with Matchers {

  override val basePath = "amf-client/shared/src/test/resources/compatibility/"

  for {
    file <- platform.fs.syncFile(basePath + "oas20").list
  } {
    // For each oas -> render raml and validate
    val path = s"oas20/$file"

    test(s"Test $path") {
      val c = CycleConfig(path, path, OasJsonHint, Raml, basePath, None, None)
      for {
        origin   <- build(c, None, useAmfJsonldSerialisation = true)
        resolved <- successful(transform(origin, c))
        rendered <- render(resolved, c, useAmfJsonldSerialization = true)
        tmp      <- writeTemporaryFile(path)(rendered)
        report   <- validate(tmp, RamlYamlHint)
      } yield {
        report.toString should include("Conforms? true")
      }
    }
  }

  // testing -> raml10
  for {
    file <- platform.fs.syncFile(basePath + "raml10").list
  } {
    // For each oas -> render raml and validate
    val path = s"raml10/$file"

    test(s"Test $path") {
      val c = CycleConfig(path, path, RamlYamlHint, Oas, basePath, None, None)
      for {
        origin   <- build(c, None, useAmfJsonldSerialisation = true)
        resolved <- successful(transform(origin, c))
        rendered <- render(resolved, c, useAmfJsonldSerialization = true)
        tmp      <- writeTemporaryFile(path)(rendered)
        report   <- validate(tmp, OasYamlHint)
      } yield {
        report.toString should include("Conforms? true")
      }
    }
  }

  private def validate(source: AsyncFile, hint: Hint): Future[AMFValidationReport] =
    Validation(platform)
      .map(_.withEnabledValidation(false))
      .flatMap { validation =>
        val config = CycleConfig(source.path, source.path, hint, hint.vendor, "", None, None)
        build(config, Some(validation), useAmfJsonldSerialisation = true).flatMap { unit =>
          hint match {
            case RamlYamlHint => validation.validate(unit, ProfileNames.RAML10)
            case OasYamlHint  => validation.validate(unit, ProfileNames.OAS20)
            case _            => Future.never
          }

        }
      }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml | Raml08 | Raml10 => CompatibilityPipeline.unhandled().resolve(unit)
    case Oas | Oas20 | Oas30    => CompatibilityPipeline.unhandled(OasProfile).resolve(unit)
    case _ => throw new IllegalArgumentException
  }
}
