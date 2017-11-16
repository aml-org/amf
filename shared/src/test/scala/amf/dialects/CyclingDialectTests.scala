package amf.dialects

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.io.BuildCycleTests
import amf.remote._
import amf.validation.Validation
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class CyclingDialectTests extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/vocabularies/"


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
    cycle("validation_dialect_uses(dialect_lib).raml", "validation_dialect_uses(dialect_lib).raml.gold", RamlYamlHint, Raml)
  }

  test("Parse Dialect with library and serialize back (references)") {
    cycle("validation_dialect_uses(dialect_lib2).raml", "validation_dialect_uses(dialect_lib2).raml", RamlYamlHint, Raml)
  }

//  test("Parse Dialect with library and serialize back (references,cycles in libraries)") {
//    cycle("validation_dialect_uses(dialect_lib3).raml", "validation_dialect_uses(dialect_lib3).raml", RamlYamlHint, Raml)
//  }

  test("Parse Dialect with library and store to json LD") {
    cycle("validation_dialect_uses(dialect_lib).raml", "validation_dialect_uses(dialect_lib).json", RamlYamlHint, Amf)
  }

  test("Parse Dialect with vocabulary and serialize back") {
    cycle("dialect_using_vocab.raml", "dialect_using_vocab.raml", RamlYamlHint, Raml)
  }


  /** Compile source with specified hint. Dump to target and assert against golden. */
  override def cycle(source: String,
            golden: String,
            hint: Hint,
            target: Vendor,
            directory: String = basePath,
            maybeValidation: Option[Validation] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory)

    build(config, maybeValidation)
      .map(map(_, config))
      .flatMap(render(_, config))
      .flatMap(content => platform.write(basePath+ golden + ".tmp", content).map((_, content)))
      .flatMap({
        case (path, actual) =>
          platform
            .resolve(config.goldenPath, None)
            .map(expected => checkDiff(actual, path, expected.stream.toString, expected.url))
      })
  }

  /** Do not render with source maps. */
  override def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions()).dumpToString
  }
}
