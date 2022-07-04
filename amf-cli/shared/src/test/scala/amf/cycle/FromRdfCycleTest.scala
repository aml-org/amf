package amf.cycle

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.io.FileAssertionTest
import amf.rdf.client.scala.RdfUnitConverter
import amf.rdf.internal.unsafe.RdfPlatformSecrets
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

/*
 * TODO: implement test also in JS.
 *  It isn't currently implemented there because of some strange errors when loading text/n3 rdf
 */
trait FromRdfCycleTest extends AsyncBeforeAndAfterEach with FileAssertionTest with Matchers with RdfPlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-cli/shared/src/test/resources/rdf/"

  test("Modular recursion") {
    cycle("modular-recursion.nt", "file://api.raml")
  }

  test("Modular recursion with types") {
    cycle("modular-recursion-with-types.nt", "file://api.raml")
  }

  test("Self encoded dialect instance example can't find model for encodes") {
    val thrown = the[Exception] thrownBy build("self-encoded-dialect-instance.nt", "file://aRandomBase")
    thrown.getMessage should include("unknown @types")
    thrown.getMessage should include("file:///myDialect.yaml#/declarations/MyVeryCoolMapping")
  }

  private def build(path: String, baseUnitId: String): Option[BaseUnit] = {
    val fullPath = basePath + path
    val content  = fs.syncFile(fullPath).read()
    val modelDoc = framework.syntaxToRdfModel("text/n3", content)
    val result = RdfUnitConverter.fromNativeRdfModel(
      baseUnitId,
      modelDoc.model,
      WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    )
    Some(result)
  }

  private def cycle(path: String, baseUnitId: String): Future[Assertion] = {
    val fullPath    = basePath + path
    val baseUnit    = build(path, baseUnitId).get
    val generatedN3 = RdfUnitConverter.toNativeRdfModel(baseUnit).toN3().split("\n").sorted.mkString("\n")
    writeTemporaryFile(fullPath)(generatedN3).flatMap(f => assertDifferences(f, fullPath))
  }
}
