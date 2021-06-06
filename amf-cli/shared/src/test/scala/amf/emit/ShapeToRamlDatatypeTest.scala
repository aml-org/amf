package amf.emit

import amf.client.environment.{WebAPIConfiguration, AsyncAPIConfiguration}
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
import amf.remod.RamlShapeSerializer.toRamlDatatype
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ShapeToRamlDatatypeTest extends AsyncFunSuite with FileAssertionTest with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val generalFindShapeFunc: BaseUnit => Option[AnyShape] = (u: BaseUnit) =>
    encodedWebApi(u)
      .flatMap(_.endPoints.headOption)
      .flatMap(_.operations.headOption)
      .flatMap(_.responses.headOption)
      .flatMap(_.payloads.headOption)
      .map(_.schema)
      .collectFirst({ case any: AnyShape => any })

  test("Test array with object items") {
    cycle("array-of-object.json", "array-of-object.raml")
  }

  test("Test array annotation type") {
    cycle("param-with-annotation.json", "param-with-annotation.raml")
  }

  test("Test parsed from json expression generations") {
    cycle("json-expression.json", "json-expression.raml")
  }

  test("Test parsed from json expression forced to build new") {
    cycle("json-expression.json",
          "json-expression-new.raml",
          generalFindShapeFunc,
          (a: AnyShape) => toRamlDatatype(a, amfConfig))
  }

  // https://github.com/aml-org/amf/issues/441
  ignore("Test recursive shape") {
    cycle("recursive.json", "recursive.raml")
  }

  test("Test shapes references") {
    cycle("reference.json", "reference.raml")
  }

  private val basePath: String   = "file://amf-cli/shared/src/test/resources/toraml/toramldatatype/source/"
  private val goldenPath: String = "amf-cli/shared/src/test/resources/toraml/toramldatatype/datatypes/"
  private val amfConfig          = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20())

  private def cycle(
      sourceFile: String,
      goldenFile: String,
      findShapeFunc: BaseUnit => Option[AnyShape] = generalFindShapeFunc,
      renderFn: AnyShape => String = (a: AnyShape) => toRamlDatatype(a, amfConfig)): Future[Assertion] = {
    val client = amfConfig.createClient()
    val ramlDatatype: Future[String] = for {
      _          <- Validation(platform)
      sourceUnit <- client.parse(basePath + sourceFile).map(_.bu)
    } yield {
      findShapeFunc(
        client
          .transform(sourceUnit, PipelineName.from(Vendor.OAS20.mediaType, TransformationPipeline.DEFAULT_PIPELINE))
          .bu)
        .map(toRamlDatatype(_, amfConfig))
        .getOrElse("")
    }
    ramlDatatype.flatMap { writeTemporaryFile(goldenFile) }.flatMap(assertDifferences(_, goldenPath + goldenFile))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
