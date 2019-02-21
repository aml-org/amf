package amf.emit

import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.UnhandledErrorHandler
import amf.core.remote.OasJsonHint
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.Oas20Plugin
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.WebApi
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ShapeToRamlDatatypeTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test array with object items") {
    val func = (u: BaseUnit) => {
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })
    }
    cycle("array-of-object.json", "array-of-object.raml", func)
  }

  // test("Test array annotation type") {
  //   val func = (u: BaseUnit) =>
  //     encodedWebApi(u)
  //       .flatMap(_.endPoints.headOption)
  //       .flatMap(_.operations.headOption)
  //       .flatMap(_.request.queryParameters.headOption)
  //       .map(_.schema)
  //       .collectFirst({ case any: AnyShape => any })
  //   cycle("param-with-annotation.json", "param-with-annotation.raml", func)
  // }

  // test("Test parsed from json expression generations") {
  //   val func = (u: BaseUnit) =>
  //     encodedWebApi(u)
  //       .flatMap(_.endPoints.headOption)
  //       .flatMap(_.operations.headOption)
  //       .flatMap(_.responses.headOption)
  //       .flatMap(_.payloads.headOption)
  //       .map(_.schema)
  //       .collectFirst({ case any: AnyShape => any })
  //   cycle("json-expression.json", "json-expression.raml", func)
  // }

  // test("Test parsed from json expression forced to build new") {
  //   val func = (u: BaseUnit) =>
  //     encodedWebApi(u)
  //       .flatMap(_.endPoints.headOption)
  //       .flatMap(_.operations.headOption)
  //       .flatMap(_.responses.headOption)
  //       .flatMap(_.payloads.headOption)
  //       .map(_.schema)
  //       .collectFirst({ case any: AnyShape => any })
  //   cycle("json-expression.json", "json-expression-new.raml", func, (a: AnyShape) => a.buildRamlDatatype())
  // }

  // test("Test recursive shape") {
  //   val func = (u: BaseUnit) =>
  //     encodedWebApi(u)
  //       .flatMap(_.endPoints.headOption)
  //       .flatMap(_.operations.headOption)
  //       .flatMap(_.responses.headOption)
  //       .flatMap(_.payloads.headOption)
  //       .map(_.schema)
  //       .collectFirst({ case any: AnyShape => any })
  //   cycle("recursive.json", "recursive.raml", func, (a: AnyShape) => a.buildRamlDatatype())
  // }

  private val basePath: String   = "file://amf-client/shared/src/test/resources/toraml/toramldatatype/source/"
  private val goldenPath: String = "amf-client/shared/src/test/resources/toraml/toramldatatype/datatypes/"

  private def cycle(sourceFile: String,
                    goldenFile: String,
                    findShapeFunc: (BaseUnit) => Option[AnyShape],
                    renderFn: (AnyShape) => String = (a: AnyShape) => a.toRamlDatatype): Future[Assertion] = {
    val ramlDatatype: Future[String] = for {
      v          <- Validation(platform)
      sourceUnit <- AMFCompiler(basePath + sourceFile, platform, OasJsonHint, v).build()
    } yield {
      findShapeFunc(Oas20Plugin.resolve(sourceUnit, UnhandledErrorHandler)).map(_.toRamlDatatype).getOrElse("")
    }
    ramlDatatype.flatMap { writeTemporaryFile(goldenFile) }.flatMap(assertDifferences(_, goldenPath + goldenFile))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
