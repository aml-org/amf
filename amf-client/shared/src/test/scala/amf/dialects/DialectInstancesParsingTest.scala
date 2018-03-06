package amf.dialects

import amf.core.remote._
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

  test("parse 5 test") {
    withDialect("dialect5.raml", "example5.raml", "example5.json", VocabularyYamlHint, Amf)
  }

  test("parse 6 test") {
    withDialect("dialect6.raml", "example6.raml", "example6.json", VocabularyYamlHint, Amf)
  }

  test("parse 7 test") {
    withDialect("dialect7.raml", "example7.raml", "example7.json", VocabularyYamlHint, Amf)
  }

  test("parse 8 test") {
    withDialect("dialect8.raml", "example8.raml", "example8.json", VocabularyYamlHint, Amf)
  }

  test("generate 1 test") {
    withDialect("dialect1.raml", "example1.json", "example1.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 2 test") {
    withDialect("dialect2.raml", "example2.json", "example2.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 3 test") {
    withDialect("dialect3.raml", "example3.json", "example3.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 4 test") {
    withDialect("dialect4.raml", "example4.json", "example4.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 5 test") {
    withDialect("dialect5.raml", "example5.json", "example5.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 6 test") {
    withDialect("dialect6.raml", "example6.json", "example6.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 7 test") {
    withDialect("dialect7.raml", "example7.json", "example7.raml", AmfJsonHint, RamlVocabulary)
  }

  test("HERE_HERE generate 8 test") {
    withDialect("dialect8.raml", "example8.json", "example8.raml", AmfJsonHint, RamlVocabulary)
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
