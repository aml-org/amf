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

  test("parse 9 test") {
    withDialect("dialect9.raml", "example9.raml", "example9.json", VocabularyYamlHint, Amf)
  }

  test("parse 10a test") {
    withDialect("dialect10.raml", "example10a.raml", "example10a.json", VocabularyYamlHint, Amf)
  }

  test("parse 10b test") {
    withDialect("dialect10.raml", "example10b.raml", "example10b.json", VocabularyYamlHint, Amf)
  }

  test("parse 10c test") {
    withDialect("dialect10.raml", "example10c.raml", "example10c.json", VocabularyYamlHint, Amf)
  }

  test("parse 11 test") {
    withDialect("dialect11.raml", "example11.raml", "example11.json", VocabularyYamlHint, Amf)
  }

  test("parse 12 test") {
    withInlineDialect("example12.raml", "example12.json", VocabularyYamlHint, Amf)
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

  test("generate 8 test") {
    withDialect("dialect8.raml", "example8.json", "example8.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 9 test") {
    withDialect("dialect9.raml", "example9.json", "example9.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 10a test") {
    withDialect("dialect10.raml", "example10a.json", "example10a.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 10b test") {
    withDialect("dialect10.raml", "example10b.json", "example10b.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 10c test") {
    withDialect("dialect10.raml", "example10c.json", "example10c.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 11 test") {
    withDialect("dialect11.raml", "example11.json", "example11.raml", AmfJsonHint, RamlVocabulary)
  }


  protected def withDialect(dialect: String,
                            source: String,
                            golden: String,
                            hint: Hint,
                            target: Vendor,
                            directory: String = basePath) = {
    for {
      v         <- Validation(platform).map(_.withEnabledValidation(false))
      something <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, v).build()
      res       <- cycle(source, golden, hint, target)
    } yield {
      res
    }
  }

  protected def withInlineDialect(source: String,
                                  golden: String,
                                  hint: Hint,
                                  target: Vendor,
                                  directory: String = basePath) = {
    for {
      v   <- Validation(platform).map(_.withEnabledValidation(false))
      res <- cycle(source, golden, hint, target)
    } yield {
      res
    }
  }
}
