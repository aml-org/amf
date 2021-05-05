package amf.emit

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.{BaseUnit, Document, Module}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.{Hint, Oas20JsonHint, Raml10YamlHint, Vendor}
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.RuntimeResolver
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.Oas20Plugin
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.api.WebApi
import amf.remod.JsonSchemaShapeSerializer.toJsonSchema
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ShapeToJsonSchemaTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test api with x-examples") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("x-examples.json", "x-examples.schema.json", func, hint = Oas20JsonHint)
  }

  test("Test api with x-examples 2") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("x-examples2.json", "x-examples2.schema.json", func, hint = Oas20JsonHint)
  }

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

    cycle("json-expression.raml", "json-expression-new.json", func, toJsonSchema)
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

    cycle("recursive.raml", "recursive.json", func, toJsonSchema)
  }

  test("Test shape id preservation") {
    val file = "shapeIdPreservation.raml"
    parse(file).map {
      case u: Module =>
        assert(
          u.declares.forall {
            case anyShape: AnyShape =>
              val originalId = anyShape.id
              toJsonSchema(anyShape)
              val newId = anyShape.id
              originalId == newId
          }
        )
    }
  }

  private val basePath: String   = "file://amf-client/shared/src/test/resources/tojson/tojsonschema/source/"
  private val goldenPath: String = "amf-client/shared/src/test/resources/tojson/tojsonschema/schemas/"

  private def parse(file: String): Future[BaseUnit] = {
    for {
      _    <- Validation(platform)
      unit <- AMFCompiler(basePath + file, platform, Raml10YamlHint, eh = UnhandledParserErrorHandler).build()
    } yield {
      unit
    }
  }

  private def cycle(file: String,
                    golden: String,
                    findShapeFunc: BaseUnit => Option[AnyShape],
                    renderFn: AnyShape => String = toJsonSchema,
                    hint: Hint = Raml10YamlHint): Future[Assertion] = {
    val jsonSchema: Future[String] = for {
      _    <- Validation(platform)
      unit <- AMFCompiler(basePath + file, platform, hint, eh = UnhandledParserErrorHandler).build()
    } yield {
      findShapeFunc(
        RuntimeResolver
          .resolve(Vendor.OAS20.name, unit, TransformationPipeline.DEFAULT_PIPELINE, UnhandledErrorHandler))
        .map(toJsonSchema)
        .getOrElse("")
    }

    jsonSchema.flatMap { writeTemporaryFile(golden) }.flatMap(assertDifferences(_, goldenPath + golden))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
