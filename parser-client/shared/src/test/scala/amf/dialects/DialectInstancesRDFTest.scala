package amf.dialects

import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectInstancesRDFTest extends AsyncFunSuite with PlatformSecrets with BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "parser-client/shared/src/test/resources/vocabularies2/instances/"

  test("RDF 1 test") {
    withDialect("dialect1.raml", "example1.raml", "example1.ttl", VocabularyYamlHint, Amf)
  }

  test("RDF 2 full test") {
    withDialectFull("dialect2.raml", "example2.raml", "example2.raml", VocabularyYamlHint, AmlVocabulary)
  }

  ignore("RDF 3 full test") {
    withDialectFull("dialect3.raml", "example3.raml", "example3.raml", VocabularyYamlHint, AmlVocabulary)
  }

  test("RDF 4 full test") {
    withDialectFull("dialect4.raml", "example4.raml", "example4.raml", VocabularyYamlHint, AmlVocabulary)
  }

  test("RDF 5 full test") {
    withDialectFull("dialect5.raml", "example5.raml", "example5.raml", VocabularyYamlHint, AmlVocabulary)
  }

  test("RDF 6 full test") {
    withDialectFull("dialect6.raml", "example6.raml", "example6.raml", VocabularyYamlHint, AmlVocabulary)
  }

  test("RDF 1 Vocabulary full test") {
    cycleFullRdf("example1.raml",
                 "example1.raml",
                 VocabularyYamlHint,
                 AmlVocabulary,
                 "parser-client/shared/src/test/resources/vocabularies2/vocabularies/")
  }

  test("RDF 1 Dialect full test") {
    cycleFullRdf("example1.raml",
                 "example1.raml",
                 VocabularyYamlHint,
                 AmlVocabulary,
                 "parser-client/shared/src/test/resources/vocabularies2/dialects/")
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
      res       <- cycleRdf(source, golden, hint, target)
    } yield {
      res
    }
  }

  protected def withDialectFull(dialect: String,
                                source: String,
                                golden: String,
                                hint: Hint,
                                target: Vendor,
                                directory: String = basePath) = {
    for {
      v         <- Validation(platform).map(_.withEnabledValidation(false))
      something <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, v).build()
      res       <- cycleFullRdf(source, golden, hint, target)
    } yield {
      res
    }
  }

}
