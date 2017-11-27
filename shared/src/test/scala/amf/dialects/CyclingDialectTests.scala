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

  test("Parse Dialect with library and serialize back (references)") {
    cycle("validation_dialect_uses(dialect_lib2).raml",
          "validation_dialect_uses(dialect_lib2).raml",
          RamlYamlHint,
          Raml)
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

  test("Vocabulary with domain") {
    cycle("withDomain.json", "withDomain.raml", AmfJsonHint, Raml)
  }
  test("Vocabulary with domain external") {
    cycle("withDomain2.raml", "withDomain2.json", RamlYamlHint, Amf)
  }

  test("Vocabulary with domain external2") {
    cycle("withDomain2.json", "withDomain2.raml", AmfJsonHint, Raml)
  }
}
