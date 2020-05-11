package amf.cycle

import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.plugin.PluginContext
import amf.facades.Validation
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/*
 * TODO: implement test also in JS.
 *  It isn't currently implemented there because of some strange errors when loading text/n3 rdf
 */
trait RdfCycleTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/rdf/"

  test("Modular recursion") {
    cycle("modular-recursion.nt", "file://api.raml")
  }

  test("Modular recursion with types") {
    cycle("modular-recursion-with-types.nt", "file://api.raml")
  }

  def cycle(path: String, baseUnitId: String): Future[Assertion] = {
    Validation(platform).flatMap(_ => {
      val fullPath     = basePath + path
      val content      = fs.syncFile(fullPath).read()
      val plugins      = PluginContext()
      val ctx          = ParserContext(eh = UnhandledParserErrorHandler, plugins = plugins)
      val rdfFramework = platform.rdfFramework.get
      val baseUnit = rdfFramework
        .syntaxToRdfModel("text/n3", content)
        .map(modelDoc => BaseUnit.fromNativeRdfModel(baseUnitId, modelDoc.model, ctx))
      val generatedN3 = baseUnit.map(_.toNativeRdfModel().toN3()).get.split("\n").sorted.mkString("\n")
      writeTemporaryFile(fullPath)(generatedN3).flatMap(f => assertDifferences(f, fullPath))
    })
  }
}
