package amf.dialects
import amf.core.remote.{Amf, Aml, VocabularyYamlHint}
import amf.core.unsafe.PlatformSecrets
import amf.io.{FunSuiteCycleTests, FunSuiteRdfCycleTests}

import scala.concurrent.ExecutionContext

class DialectRDFTest extends FunSuiteRdfCycleTests with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val productionPath                                       = "amf-client/shared/src/test/resources/vocabularies2/production/"

  override def basePath: String = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

  test("RDF 1 test") {
    cycleFullRdf("example1.raml", "example1.raml", VocabularyYamlHint, Aml, basePath)
  }

  test("RDF 2 test") {
    cycleFullRdf("example2.raml", "example2.raml", VocabularyYamlHint, Aml, basePath)
  }

  test("RDF 3 test") {
    cycleFullRdf("example3.raml", "example3.jsonld", VocabularyYamlHint, Amf, basePath)
  }

  test("RDF 13 test") {
    cycleFullRdf("example13.raml", "example13.jsonld", VocabularyYamlHint, Amf, basePath)
  }

  test("RDF Production system2 dialect ex1  test") {
    cycleFullRdf("dialectex1.raml", "dialectex1.jsonld", VocabularyYamlHint, Amf, productionPath + "system2/")
  }

  test("RDF Production system2 dialect ex2  test") {
    cycleFullRdf("dialectex2.raml", "dialectex2.jsonld", VocabularyYamlHint, Amf, productionPath + "system2/")
  }
}
