package amf.emit

import amf.client.environment.WebAPIConfiguration
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.{RenderOptions, ShapeRenderOptions}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, Module}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.{Hint, Oas20JsonHint, Raml10YamlHint, Vendor}
import amf.core.internal.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.plugins.domain.apicontract.models.api.WebApi
import amf.plugins.domain.shapes.models.AnyShape
import amf.remod.JsonSchemaShapeSerializer.toJsonSchema
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ShapeToJsonSchemaTest extends AsyncFunSuite with FileAssertionTest with PlatformSecrets {

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
    val file   = "shapeIdPreservation.raml"
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    parse(file, config).map {
      case u: Module =>
        assert(
          u.declares.forall {
            case anyShape: AnyShape =>
              val originalId = anyShape.id
              toJsonSchema(anyShape, config)
              val newId = anyShape.id
              originalId == newId
          }
        )
    }
  }

  private val basePath: String   = "file://amf-cli/shared/src/test/resources/tojson/tojsonschema/source/"
  private val goldenPath: String = "amf-cli/shared/src/test/resources/tojson/tojsonschema/schemas/"

  private def parse(file: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val client = config.createClient()
    for {
      _    <- Validation(platform)
      unit <- client.parse(basePath + file).map(_.bu)
    } yield {
      unit
    }
  }

  private def cycle(file: String,
                    golden: String,
                    findShapeFunc: BaseUnit => Option[AnyShape],
                    renderFn: AnyShape => String = toJsonSchema,
                    hint: Hint = Raml10YamlHint): Future[Assertion] = {
    val config = WebAPIConfiguration
      .WebAPI()
      .withRenderOptions(RenderOptions().withShapeRenderOptions(ShapeRenderOptions().withoutCompactedEmission))
    val jsonSchema: Future[String] = for {
      unit <- parse(file, config)
    } yield {
      findShapeFunc(
        config
          .createClient()
          .transform(unit, PipelineName.from(Vendor.OAS20.name, TransformationPipeline.DEFAULT_PIPELINE))
          .bu
      ).map { element =>
          toJsonSchema(element, config)
        }
        .getOrElse("")
    }

    jsonSchema.flatMap { writeTemporaryFile(golden) }.flatMap(assertDifferences(_, goldenPath + golden))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
