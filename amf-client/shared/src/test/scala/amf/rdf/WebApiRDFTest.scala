package amf.rdf
import amf.core.remote.{Aml, Raml, RamlYamlHint, VocabularyYamlHint}
import amf.core.unsafe.PlatformSecrets
import amf.io.FunSuiteCycleTests

import scala.concurrent.ExecutionContext

class WebApiRDFTest extends FunSuiteCycleTests with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val basePath                                             = "amf-client/shared/src/test/resources/production/raml10/"

  test("RDF 1 Dialect full test") {
    cycleFullRdf("demo-api/api-short.raml", "demo-api/api-short.raml", RamlYamlHint, Raml, basePath)
  }
}
