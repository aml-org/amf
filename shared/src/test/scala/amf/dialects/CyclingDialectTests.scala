package amf.dialects

import amf.client.GenerationOptions
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.io.BuildCycleTests
import amf.remote._

import scala.concurrent.{ExecutionContext, Future}

class CyclingDialectTests extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "shared/src/test/resources/vocabularies/"

  test("Parse Dialect with include in ref") {
    cycle("validation_dialect_using_fragments3.raml", "validation_dialect_fragment3.json", RamlYamlHint, Amf)
  }

  test("Parse Dialect with include in ref and serialize back") {
    cycle("validation_dialect_using_fragments3.raml", "validation_dialect_using_fragments3.raml", RamlYamlHint, Raml)
  }

  test("Parse Dialect with include in node definition") {
    cycle("validation_dialect_using_fragments4.raml", "validation_dialect_fragment4.json", RamlYamlHint, Amf)
  }

  test("Parse Dialect with include in node definition and serialize back") {
    cycle("validation_dialect_using_fragments4.raml", "validation_dialect_using_fragments4.raml", RamlYamlHint, Raml)
  }

  test("Parse Dialect with library and serialize back") {
    cycle("validation_dialect_uses(dialect_lib).raml",
          "validation_dialect_uses(dialect_lib).raml.gold",
          RamlYamlHint,
          Raml)
  }

  test("Parse Dialect with library and store to json LD") {
    cycle("validation_dialect_uses(dialect_lib).raml", "validation_dialect_uses(dialect_lib).json", RamlYamlHint, Amf)
  }

  /** Do not render with source maps. */
  override def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions()).dumpToString
  }
}
