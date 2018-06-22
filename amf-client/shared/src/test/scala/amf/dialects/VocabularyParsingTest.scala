package amf.dialects

import amf.core.remote.{Amf, AmfJsonHint, AmlVocabulary, VocabularyYamlHint}
import amf.io.BuildCycleTests

import scala.concurrent.ExecutionContext

class VocabularyParsingTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/vocabularies/"

  test("parse 1 test") {
    cycle("example1.raml", "example1.json", VocabularyYamlHint, Amf)
  }

  test("parse 2 test") {
    cycle("example2.raml", "example2.json", VocabularyYamlHint, Amf)
  }

  test("parse 3 test") {
    cycle("example3.raml", "example3.json", VocabularyYamlHint, Amf)
  }

  test("parse 4 test") {
    cycle("example4.raml", "example4.json", VocabularyYamlHint, Amf)
  }

  test("parse 5 test") {
    cycle("example5.raml", "example5.json", VocabularyYamlHint, Amf)
  }

  test("parse 6 test") {
    cycle("example6.raml", "example6.json", VocabularyYamlHint, Amf)
  }

  test("generate 1 test") {
    cycle("example1.json", "example1.raml", AmfJsonHint, AmlVocabulary)
  }

  test("generate 2 test") {
    cycle("example2.json", "example2.raml", AmfJsonHint, AmlVocabulary)
  }

  test("generate 3 test") {
    cycle("example3.json", "example3.raml", AmfJsonHint, AmlVocabulary)
  }

  test("generate 4 test") {
    cycle("example4.json", "example4.raml", AmfJsonHint, AmlVocabulary)
  }

  test("generate 5 test") {
    cycle("example5.json", "example5.raml", AmfJsonHint, AmlVocabulary)
  }

  test("generate 6 test") {
    cycle("example6.json", "example6.raml", AmfJsonHint, AmlVocabulary)
  }
}
