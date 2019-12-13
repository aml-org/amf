package amf.emit

import amf.core.annotations.SourceAST
import amf.core.model.document.{ExternalFragment, BaseUnit}
import amf.core.parser.ParserContext
import amf.core.remote.RamlYamlHint
import amf.facades.{Validation, AMFCompiler}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.contexts.parser.raml.Raml10WebApiContext
import amf.plugins.document.webapi.parser.spec.domain.{RamlExamplesParser, DefaultExampleOptions}
import amf.plugins.domain.shapes.models.{Example, AnyShape}
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YMap, YDocument}

import scala.concurrent.{Future, ExecutionContext}

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
    for {
      v       <- Validation(platform)
      unit    <- AMFCompiler(basePath + source, platform, RamlYamlHint, v).build()
      example <- findExample(unit, removeRaw)
      temp    <- writeTemporaryFile(golden)(example.toJson)
      r       <- assertDifferences(temp, goldenPath + golden)
    } yield {
      r
    }
  }

  private def findExample(unit: BaseUnit, removeRaw: Boolean): Future[Example] = unit match {
    case f: ExternalFragment =>
      val sourceAst: Option[SourceAST] = unit.annotations.find(_.isInstanceOf[SourceAST])
      sourceAst match {
        case Some(a) =>
          val ast     = a.ast.asInstanceOf[YDocument].as[YMap]
          val context = new Raml10WebApiContext("", Nil, ParserContext())
          val examples =
            RamlExamplesParser(ast, "example", "examples", None, AnyShape.apply().withExample, DefaultExampleOptions)(
              context).parse()
          Future.successful(examples.head)
        case None => Future.failed(fail("Not a named example fragment"))
      }
    case _ => Future.failed(fail("Not a named example fragment"))
  }

  private val basePath: String   = "file://amf-client/shared/src/test/resources/tojson/examples/source/"
  private val goldenPath: String = "amf-client/shared/src/test/resources/tojson/examples/generated/"
}
