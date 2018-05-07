package amf.dialects

import amf.core.model.document.BaseUnit
import amf.core.remote.{RamlVocabulary, VocabularyYamlHint}
import amf.io.BuildCycleTests
import amf.plugins.document.vocabularies.VocabulariesPlugin

import scala.concurrent.ExecutionContext

abstract class DialectResolutionCycleTests extends BuildCycleTests {
  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    VocabulariesPlugin.resolve(unit)
  }
}

class DialectsResolutionTest extends DialectResolutionCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

  test("resolve include test") {
    cycle("example9.raml", "example9.resolved.raml", VocabularyYamlHint, RamlVocabulary)
  }

  test("resolve library test") {
    cycle("example7.raml", "example7.resolved.raml", VocabularyYamlHint, RamlVocabulary)
  }

  test("resolve 13 test") {
    cycle("example13.raml", "example13.resolved.raml", VocabularyYamlHint, RamlVocabulary)
  }
}
