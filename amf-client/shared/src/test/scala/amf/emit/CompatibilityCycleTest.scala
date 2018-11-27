package amf.emit

import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.RamlCompatibilityPipeline
import org.mulesoft.common.io.AsyncFile

import scala.concurrent.Future

class CompatibilityCycleTest extends FunSuiteCycleTests {

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
        _        <- validate(tmp, RamlYamlHint)
      } yield {
        succeed
      }
    }
  }

  private def validate(source: AsyncFile, hint: Hint): Future[BaseUnit] = {
    println(s"About to validate ${source.path}")
    val config = CycleConfig(source.path, source.path, hint, hint.vendor, "", None)
    build(config, None, useAmfJsonldSerialisation = true)
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml | Raml08 | Raml10 => RamlCompatibilityPipeline.unhandled.resolve(unit)
    case Oas | Oas20 | Oas30    => ???
  }
}
