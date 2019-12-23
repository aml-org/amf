package amf.dialects

import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.io.FunSuiteCycleTests

import scala.concurrent.ExecutionContext

class DialectInstancesRDFTest extends FunSuiteCycleTests with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath       = "amf-client/shared/src/test/resources/vocabularies2/instances/"
  val productionPath = "amf-client/shared/src/test/resources/vocabularies2/production/"

  test("RDF 1 test") {
    withDialect("dialect1.raml", "example1.raml", "example1.ttl", VocabularyYamlHint, Amf)
  }

  test("RDF 2 full test") {
    withDialectFull("dialect2.raml", "example2.raml", "example2.raml", VocabularyYamlHint, Aml)
  }

  ignore("RDF 3 full test") {
    withDialectFull("dialect3.raml", "example3.raml", "example3.raml", VocabularyYamlHint, Aml)
  }

  test("RDF 4 full test") {
    withDialectFull("dialect4.raml", "example4.raml", "example4.raml", VocabularyYamlHint, Aml)
  }

  test("RDF 5 full test") {
    withDialectFull("dialect5.raml", "example5.raml", "example5.raml", VocabularyYamlHint, Aml)
  }

  test("RDF 6 full test") {
    withDialectFull("dialect6.raml", "example6.raml", "example6.raml", VocabularyYamlHint, Aml)
  }

  test("RDF 26 full test") {
    withDialectFull("dialect26.raml", "example26.raml", "example26.raml", VocabularyYamlHint, Aml)
  }

  test("RDF 1 Vocabulary full test") {
    cycleFullRdf("example1.raml",
                 "example1.raml",
                 VocabularyYamlHint,
                 Aml,
                 "amf-client/shared/src/test/resources/vocabularies2/vocabularies/")
  }

  test("RDF 1 Dialect full test") {
    cycleFullRdf("example1.raml",
                 "example1.raml",
                 VocabularyYamlHint,
                 Aml,
                 "amf-client/shared/src/test/resources/vocabularies2/dialects/")
  }

  test("EngDemos vocabulary test") {
    cycleFullRdf("eng_demos.yaml",
                 "eng_demos.yaml",
                 VocabularyYamlHint,
                 Aml,
                 "amf-client/shared/src/test/resources/vocabularies2/production/")
  }

  test("Container Configuration 0.2 ex1 test") {
    withDialectFull("dialect.raml",
                    "ex1.raml",
                    "ex1.raml",
                    VocabularyYamlHint,
                    Aml,
                    "amf-client/shared/src/test/resources/vocabularies2/production/system2/")
  }

  test("Container Configuration 0.2 ex2 test") {
    withDialectFull("dialect.raml",
                    "ex2.raml",
                    "ex2.raml",
                    VocabularyYamlHint,
                    Aml,
                    "amf-client/shared/src/test/resources/vocabularies2/production/system2/")
  }

  private def withDialect(dialect: String,
                          source: String,
                          golden: String,
                          hint: Hint,
                          target: Vendor,
                          directory: String = basePath) = {
    for {
      _ <- Validation(platform)
      _ <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, eh = UnhandledParserErrorHandler)
        .build()
      res <- cycleRdf(source, golden, hint, target)
    } yield {
      res
    }
  }

  private def withDialectFull(dialect: String,
                              source: String,
                              golden: String,
                              hint: Hint,
                              target: Vendor,
                              directory: String = basePath) = {
    for {
      _ <- Validation(platform)
      _ <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, eh = UnhandledParserErrorHandler)
        .build()
      res <- cycleFullRdf(source, golden, hint, target, directory)
    } yield {
      res
    }
  }

}
