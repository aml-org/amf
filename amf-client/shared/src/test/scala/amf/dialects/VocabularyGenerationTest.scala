package amf.dialects

import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml, RamlYamlHint}
import amf.facades.AMFDumper
import amf.io.BuildCycleTests

import scala.concurrent.ExecutionContext

class VocabularyGenerationTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies/"

  test("Parse Vocabulary") {
    cycle("raml_async.raml", "raml_async.json", RamlYamlHint, Amf)
  }

  test("Store Vocabulary") {
    cycle("raml_async.raml", "raml_async-gold.raml", RamlYamlHint, Raml)
  }
  test("Parse Dialect") {
    cycle("validation_dialect.raml", "validation_dialect.json", RamlYamlHint, Amf)
  }

  test("Store Dialect") {
    cycle("validation_dialect.raml", "validation_dialect-gold.raml", RamlYamlHint, Raml)
  }

  /** Return random temporary file name for testing. */
  // override def tmp(name: String = ""): String = basePath +  name +".tmp"


  test("Store Dialect 2") {
    cycle("mule_config_dialect.raml", "mule_config_dialect_gold.raml", RamlYamlHint, Raml)
  }

  test("Store Dialect 3") {
    cycle("validation_dialect_fixed2.raml", "validation_dialect_fixed2_gold.raml", RamlYamlHint, Raml)
  }

  /** Do not render with source maps. */
  override def render(unit: BaseUnit, config: CycleConfig): String = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions()).dumpToString
  }
}
