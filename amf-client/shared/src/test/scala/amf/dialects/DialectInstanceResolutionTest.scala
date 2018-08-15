package amf.dialects

import amf.core.model.document.BaseUnit
import amf.core.remote.{Hint, Aml, Vendor, VocabularyYamlHint}
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests
import amf.plugins.document.vocabularies.AMLPlugin

import scala.concurrent.ExecutionContext

abstract class DialectInstanceResolutionCycleTests extends BuildCycleTests {
  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    AMLPlugin.resolve(unit)
}

class DialectInstanceResolutionTest extends DialectInstanceResolutionCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/instances/"

  test("resolve fragment test") {
    withDialect("dialect8.raml", "example8.raml", "example8.resolved.raml", VocabularyYamlHint, Aml)
  }

  test("resolve library test") {
    withDialect("dialect9.raml", "example9.raml", "example9.resolved.raml", VocabularyYamlHint, Aml)
  }

  protected def withDialect(dialect: String,
                            source: String,
                            golden: String,
                            hint: Hint,
                            target: Vendor,
                            directory: String = basePath) = {
    for {
      v   <- Validation(platform).map(_.withEnabledValidation(false))
      _   <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, v).build()
      res <- cycle(source, golden, hint, target)
    } yield {
      res
    }
  }

}
