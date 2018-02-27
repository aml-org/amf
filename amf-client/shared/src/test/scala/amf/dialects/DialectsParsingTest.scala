package amf.dialects

import amf.core.remote.{Amf, AmfJsonHint, RamlVocabulary, VocabularyYamlHint}
import amf.io.BuildCycleTests

import scala.concurrent.ExecutionContext

class DialectsParsingTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

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

  test("parse 7 test") {
    cycle("example7.raml", "example7.json", VocabularyYamlHint, Amf)
  }

  test("parse 8 test") {
    cycle("example8.raml", "example8.json", VocabularyYamlHint, Amf)
  }

  test("parse 9 test") {
    cycle("example9.raml", "example9.json", VocabularyYamlHint, Amf)
  }

  test("parse mappings_lib test") {
    cycle("mappings_lib.raml", "mappings_lib.json", VocabularyYamlHint, Amf)
  }

  test("generate 1 test") {
    cycle("example1.json", "example1.raml",  AmfJsonHint, RamlVocabulary)
  }

  test("generate 2 test") {
    cycle("example2.json", "example2.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 3 test") {
    cycle("example3.json", "example3.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 4 test") {
    cycle("example4.json", "example4.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 5 test") {
    cycle("example5.json", "example5.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 6 test") {
    cycle("example6.json", "example6.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 7 test") {
    cycle("example7.json", "example7.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate 8 test") {
    cycle("example8.json", "example8.raml", AmfJsonHint, RamlVocabulary)
  }

  test("generate mappings_lib test") {
    cycle("mappings_lib.json", "mappings_lib.raml", AmfJsonHint, RamlVocabulary)
  }

}
