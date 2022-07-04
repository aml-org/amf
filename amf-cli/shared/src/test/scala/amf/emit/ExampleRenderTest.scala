package amf.emit

import amf.aml.internal.registries.AMLRegistry
import amf.apicontract.client.scala.WebAPIConfiguration
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.raml.parser.context.Raml10WebApiContext
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.{SourceAST, SourceYPart}
import amf.core.internal.parser.LimitedParseConfig
import amf.core.internal.remote.Mimes._
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.model.domain.{AnyShape, Example}
import amf.shapes.internal.spec.common.parser.{DefaultExampleOptions, RamlExamplesParser}
import org.scalatest.funsuite.AsyncFunSuite
import org.yaml.model.{YDocument, YMap}

import scala.concurrent.{ExecutionContext, Future}

class ExampleRenderTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Simple yaml scalar example") {
    cycle("simple-yaml-scalar.raml", "simple-yaml-scalar.yaml", mediaType = `application/yaml`)
  }

  test("Simple yaml scalar example without raw") {
    cycle("simple-yaml-scalar.raml", "simple-yaml-scalar.yaml", removeRaw = true, mediaType = `application/yaml`)
  }

  test("Simple yaml object example") {
    cycle("simple-yaml-object.raml", "simple-yaml-object.yaml", mediaType = `application/yaml`)
  }

  test("Simple yaml object example without raw") {
    cycle("simple-yaml-object.raml", "simple-yaml-object.yaml", removeRaw = true, mediaType = `application/yaml`)
  }

  test("Simple yaml array example") {
    cycle("simple-yaml-array.raml", "simple-yaml-array.yaml", mediaType = `application/yaml`)
  }

  test("Simple yaml array example without raw") {
    cycle("simple-yaml-array.raml", "simple-yaml-array.yaml", removeRaw = true, mediaType = `application/yaml`)
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
    cycle("xml-example.raml", "xml-example.xml", mediaType = `application/xml`)
  }

  test("Render yaml example to json without raw") {
    cycle("simple-yaml-object.raml", "yaml-object-as-json.json", removeRaw = true, mediaType = `application/json`)
  }

  test("Render yaml example to json") {
    cycle("simple-yaml-object.raml", "yaml-object-as-json.json", mediaType = `application/json`)
  }

  private def cycle(
      source: String,
      golden: String,
      removeRaw: Boolean = false,
      mediaType: String = `application/json`
  ) = {
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    for {
      unit    <- config.baseUnitClient().parse(basePath + source).map(_.baseUnit)
      example <- findExample(unit, removeRaw)
      temp    <- writeTemporaryFile(golden)(config.elementClient().renderExample(example, mediaType))
      r       <- assertDifferences(temp, goldenPath + golden)
    } yield {
      r
    }
  }

  private def findExample(unit: BaseUnit, removeRaw: Boolean): Future[Example] = unit match {
    case _: ExternalFragment =>
      val sourceAst: Option[SourceYPart] = unit.annotations.find(_.isInstanceOf[SourceYPart])
      sourceAst match {
        case Some(a) =>
          val ast = a.ast.asInstanceOf[YDocument].as[YMap]
          val context =
            new Raml10WebApiContext(
              "",
              Nil,
              ParserContext(config = LimitedParseConfig(DefaultErrorHandler(), AMLRegistry.empty))
            )
          val anyShape = AnyShape()
          RamlExamplesParser(ast, "example", "examples", anyShape, DefaultExampleOptions)(
            WebApiShapeParserContextAdapter(context)
          ).parse()
          Future.successful(anyShape.examples.head)
        case None => Future.failed(fail("Not a named example fragment"))
      }
    case _ => Future.failed(fail("Not a named example fragment"))
  }

  private val basePath: String   = "file://amf-cli/shared/src/test/resources/tojson/examples/source/"
  private val goldenPath: String = "amf-cli/shared/src/test/resources/tojson/examples/generated/"
}
