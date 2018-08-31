package amf.emit

import amf.core.model.document.{BaseUnit, Document}
import amf.core.remote.RamlYamlHint
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.Oas20Plugin
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.WebApi
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.Future

class ShapeToJsonSchemaTest extends AsyncFunSuite with FileAssertionTest {

  test("Test array with object items") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("array-of-object.raml", "array-of-object.json", func)
  }

  test("Test array annotation type") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.request.queryParameters.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("param-with-annotation.raml", "param-with-annotation.json", func)
  }

  test("Test parsed from json expression generations") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("json-expression.raml", "json-expression.json", func)
  }

  test("Test parsed from json expression forced to build new") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("json-expression.raml", "json-expression-new.json", func, (a: AnyShape) => a.buildJsonSchema())
  }

  test("Test recursive shape") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("recursive.raml", "recursive.json", func, (a: AnyShape) => a.buildJsonSchema())
  }

  private val basePath: String   = "file://amf-client/shared/src/test/resources/tojson/tojsonschema/source/"
  private val goldenPath: String = "amf-client/shared/src/test/resources/tojson/tojsonschema/schemas/"

  private def cycle(file: String,
                    golden: String,
                    findShapeFunc: (BaseUnit) => Option[AnyShape],
                    renderFn: (AnyShape) => String = (a: AnyShape) => a.toJsonSchema): Future[Assertion] = {
    val jsonSchema: Future[String] = for {
      v    <- Validation(platform)
      unit <- AMFCompiler(basePath + file, platform, RamlYamlHint, v).build()
    } yield {
      findShapeFunc(Oas20Plugin.resolve(unit)).map(_.toJsonSchema).getOrElse("")
    }

    jsonSchema.flatMap { writeTemporaryFile(golden) }.flatMap(assertDifferences(_, goldenPath + golden))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
