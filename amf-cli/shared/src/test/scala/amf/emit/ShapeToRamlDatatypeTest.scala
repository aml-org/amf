package amf.emit

import amf.apicontract.client.scala.{APIConfiguration, AsyncAPIConfiguration, RAMLConfiguration, WebAPIConfiguration}
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.remote.Spec
import amf.core.internal.unsafe.PlatformSecrets
import amf.io.FileAssertionTest
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.render.RamlShapeRenderer.toRamlDatatype
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
          (a: AnyShape) => toRamlDatatype(a, parseConfig))
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
  private val parseConfig        = APIConfiguration.API()
  private val renderConfig       = RAMLConfiguration.RAML10()

  private def cycle(
      sourceFile: String,
      goldenFile: String,
      findShapeFunc: BaseUnit => Option[AnyShape] = generalFindShapeFunc,
      renderFn: AnyShape => String = (a: AnyShape) => toRamlDatatype(a, parseConfig)): Future[Assertion] = {
    val client       = parseConfig.baseUnitClient()
    val renderClient = renderConfig.baseUnitClient()
    val ramlDatatype: Future[String] = for {
      sourceUnit <- client.parse(basePath + sourceFile).map(_.baseUnit)
    } yield {
      findShapeFunc(renderClient.transform(sourceUnit, PipelineId.Default).baseUnit)
        .map(toRamlDatatype(_, renderConfig))
        .getOrElse("")
    }
    ramlDatatype.flatMap { writeTemporaryFile(goldenFile) }.flatMap(assertDifferences(_, goldenPath + goldenFile))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
