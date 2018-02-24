package amf.dialects

import amf.core.remote.{Amf, AmfJsonHint, RamlVocabulary, VocabularyYamlHint}
import amf.io.BuildCycleTests

import scala.concurrent.ExecutionContext

class DialectsParsingTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

  test("HERE_HERE parse 1 test") {
    cycle("example1.raml", "example1.json", VocabularyYamlHint, Amf)
  }

  test("HERE_HERE_HERE parse 2 test") {
    cycle("example2.raml", "example2.json", VocabularyYamlHint, Amf)
  }

  test("HERE_HERE generate 1 test") {
    cycle("example1.json", "example1.raml",  AmfJsonHint, RamlVocabulary)
  }

  test("HERE_HERE_HERE generate 2 test") {
    cycle("example2.json", "example2.raml",  AmfJsonHint, RamlVocabulary)
  }

}
