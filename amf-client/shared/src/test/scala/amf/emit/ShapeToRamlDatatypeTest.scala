package amf.emit

import amf.client.parse.DefaultParserErrorHandler
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.remote.{Oas20JsonHint, Vendor}
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.RuntimeResolver
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.api.WebApi
import amf.remod.RamlShapeSerializer
import amf.remod.RamlShapeSerializer.toRamlDatatype
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ShapeToRamlDatatypeTest extends AsyncFunSuite with FileAssertionTest {

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
    cycle("json-expression.json", "json-expression-new.raml", generalFindShapeFunc, (a: AnyShape) => toRamlDatatype(a))
  }

  // https://github.com/aml-org/amf/issues/441
  ignore("Test recursive shape") {
    cycle("recursive.json", "recursive.raml")
  }

  test("Test shapes references") {
    cycle("reference.json", "reference.raml")
  }

  private val basePath: String   = "file://amf-client/shared/src/test/resources/toraml/toramldatatype/source/"
  private val goldenPath: String = "amf-client/shared/src/test/resources/toraml/toramldatatype/datatypes/"

  private def cycle(sourceFile: String,
                    goldenFile: String,
                    findShapeFunc: BaseUnit => Option[AnyShape] = generalFindShapeFunc,
                    renderFn: AnyShape => String = (a: AnyShape) => toRamlDatatype(a)): Future[Assertion] = {
    val ramlDatatype: Future[String] = for {
      _ <- Validation(platform)
      sourceUnit <- AMFCompiler(basePath + sourceFile, platform, Oas20JsonHint, eh = DefaultParserErrorHandler())
        .build()
    } yield {
      findShapeFunc(
        RuntimeResolver.resolve(Vendor.OAS20.name,
                                sourceUnit,
                                TransformationPipeline.DEFAULT_PIPELINE,
                                UnhandledErrorHandler)).map(toRamlDatatype).getOrElse("")
    }
    ramlDatatype.flatMap { writeTemporaryFile(goldenFile) }.flatMap(assertDifferences(_, goldenPath + goldenFile))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
