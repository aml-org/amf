package amf.cycle

import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.plugin.PluginContext
import amf.facades.Validation
import amf.io.FileAssertionTest
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

/*
 * TODO: implement test also in JS.
 *  It isn't currently implemented there because of some strange errors when loading text/n3 rdf
 */
trait FromRdfCycleTest extends AsyncFunSuite with FileAssertionTest with AsyncBeforeAndAfterEach with Matchers{

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/rdf/"

  test("Modular recursion") {
    cycle("modular-recursion.nt", "file://api.raml")
  }

  test("Modular recursion with types") {
    cycle("modular-recursion-with-types.nt", "file://api.raml")
  }

  test("Self encoded dialect instance example can't find model for encodes") {
    val thrown = the [Exception] thrownBy build("self-encoded-dialect-instance.nt", "file://aRandomBase")
    thrown.getMessage should include("unknown @types")
    thrown.getMessage should include("file:///myDialect.yaml#/declarations/MyVeryCoolMapping")
  }

  override protected def beforeEach(): Future[Unit] = Validation(platform).map(_ => Unit)

  private def build(path: String, baseUnitId: String): Option[BaseUnit] = {
    val fullPath = basePath + path
    val content = fs.syncFile(fullPath).read()
    val plugins = PluginContext()
    val ctx = ParserContext(eh = UnhandledParserErrorHandler, plugins = plugins)
    val rdfFramework = platform.rdfFramework.get
    rdfFramework
      .syntaxToRdfModel("text/n3", content)
      .map(modelDoc => BaseUnit.fromNativeRdfModel(baseUnitId, modelDoc.model, ctx))
  }

  private def cycle(path: String, baseUnitId: String): Future[Assertion] = {
    val fullPath = basePath + path
    val baseUnit = build(path, baseUnitId).get
    val generatedN3 = baseUnit.toNativeRdfModel().toN3().split("\n").sorted.mkString("\n")
    writeTemporaryFile(fullPath)(generatedN3).flatMap(f => assertDifferences(f, fullPath))
  }
}
