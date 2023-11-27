package amf.cycle

import amf.apicontract.client.scala.OASConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.io.FileAssertionTest
import amf.rdf.client.scala.RdfUnitConverter

import scala.concurrent.Future

class RdfModelRoundTripTest extends FileAssertionTest {

  test("oas 20 api rdf round trip results in correct model") {
    val src    = "file://amf-cli/shared/src/test/resources/rdf/apis/banking.json"
    val golden = "file://amf-cli/shared/src/test/resources/rdf/apis/banking.jsonld"

    val client = OASConfiguration.OAS20().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()
    val rendered: Future[String] = client.parse(src) map { result =>
      val rdfModel = RdfUnitConverter.toNativeRdfModel(result.baseUnit)
      val roundTripBaseUnit =
        RdfUnitConverter.fromNativeRdfModel(result.baseUnit.id, rdfModel, OASConfiguration.OAS20())
      client.render(roundTripBaseUnit, "application/ld+json")
    }
    rendered.flatMap(content => writeTemporaryFile(golden)(content)).flatMap(assertDifferences(_, golden))
  }
}
