package amf.dialects

import amf.core.remote.{Amf, VocabularyYamlHint}
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
}
