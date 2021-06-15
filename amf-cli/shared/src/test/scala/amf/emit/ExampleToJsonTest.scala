package amf.emit

import amf.client.environment.WebAPIConfiguration
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.SourceAST
import amf.core.internal.parser.ParseConfiguration
import amf.io.FileAssertionTest
import amf.plugins.document.apicontract.contexts.parser.raml.Raml10WebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.domain.{DefaultExampleOptions, RamlExamplesParser}
import amf.plugins.domain.shapes.models.{AnyShape, Example}
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YDocument, YMap}

import scala.concurrent.{ExecutionContext, Future}

class ExampleToJsonTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  test("Simple yaml scalar example") {
    cycle("simple-yaml-scalar.raml", "simple-yaml-scalar.json")
  }

  test("Simple yaml scalar example without raw") {
    cycle("simple-yaml-scalar.raml", "simple-yaml-scalar.json", removeRaw = true)
  }

  test("Simple yaml object example") {
    cycle("simple-yaml-object.raml", "simple-yaml-object.json")
  }

  test("Simple yaml object example without raw") {
    cycle("simple-yaml-object.raml", "simple-yaml-object.json", removeRaw = true)
  }

  test("Simple yaml array example") {
    cycle("simple-yaml-array.raml", "simple-yaml-array.json")
  }

  test("Simple yaml array example without raw") {
    cycle("simple-yaml-array.raml", "simple-yaml-array.json", removeRaw = true)
  }

  test("Json object example") {
    cycle("json-object.raml", "json-object-raw.json")
  }

  ignore("Json object example without raw") {
    cycle("json-object.raml", "json-object.json", removeRaw = true)
  }

  test("Json array example") {
    cycle("json-array.raml", "json-array-raw.json")
  }

  ignore("Json array example without raw") {
    cycle("json-array.raml", "json-array.json", removeRaw = true)
  }

  test("Xml example") {
    cycle("xml-example.raml", "xml-example.json")
  }

  private def cycle(source: String, golden: String, removeRaw: Boolean = false): Future[Assertion] = {
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    for {
      unit    <- config.createClient().parse(basePath + source).map(_.bu)
      example <- findExample(unit, removeRaw)
      temp    <- writeTemporaryFile(golden)(example.toJson(config))
      r       <- assertDifferences(temp, goldenPath + golden)
    } yield {
      r
    }
  }

  private def findExample(unit: BaseUnit, removeRaw: Boolean): Future[Example] = unit match {
    case _: ExternalFragment =>
      val sourceAst: Option[SourceAST] = unit.annotations.find(_.isInstanceOf[SourceAST])
      sourceAst match {
        case Some(a) =>
          val ast = a.ast.asInstanceOf[YDocument].as[YMap]
          val context =
            new Raml10WebApiContext("", Nil, ParserContext(config = ParseConfiguration(DefaultErrorHandler())))
          val anyShape = AnyShape()
          RamlExamplesParser(ast, "example", "examples", anyShape, DefaultExampleOptions)(
            WebApiShapeParserContextAdapter(context)).parse()
          Future.successful(anyShape.examples.head)
        case None => Future.failed(fail("Not a named example fragment"))
      }
    case _ => Future.failed(fail("Not a named example fragment"))
  }

  private val basePath: String   = "file://amf-cli/shared/src/test/resources/tojson/examples/source/"
  private val goldenPath: String = "amf-cli/shared/src/test/resources/tojson/examples/generated/"
}
