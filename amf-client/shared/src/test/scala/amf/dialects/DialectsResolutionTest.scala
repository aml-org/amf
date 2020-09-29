package amf.dialects

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Aml, VocabularyYamlHint}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.vocabularies.AMLPlugin

import scala.concurrent.ExecutionContext

abstract class DialectResolutionCycleTests extends FunSuiteCycleTests {
  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    AMLPlugin().resolve(unit, UnhandledErrorHandler)
}

class DialectsResolutionTest extends DialectResolutionCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

  test("resolve include test") {
    cycle("example9.yaml", "example9.resolved.yaml", VocabularyYamlHint, Aml)
  }

  test("resolve 13 test") {
    cycle("example13.yaml", "example13.resolved.yaml", VocabularyYamlHint, Aml)
  }
}
