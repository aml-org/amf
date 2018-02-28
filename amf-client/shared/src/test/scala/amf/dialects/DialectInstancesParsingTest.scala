package amf.dialects

import amf.core.remote.{Amf, Hint, Vendor, VocabularyYamlHint}
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests

import scala.concurrent.ExecutionContext

class DialectInstancesParsingTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/instances/"


  test("parse 1 test") {
    withDialect("dialect1.raml", "example1.raml", "example1.json", VocabularyYamlHint, Amf)
  }

  test("parse 2 test") {
    withDialect("dialect2.raml", "example2.raml", "example2.json", VocabularyYamlHint, Amf)
  }

  test("parse 3 test") {
    withDialect("dialect3.raml", "example3.raml", "example3.json", VocabularyYamlHint, Amf)
  }

  test("parse 4 test") {
    withDialect("dialect4.raml", "example4.raml", "example4.json", VocabularyYamlHint, Amf)
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
