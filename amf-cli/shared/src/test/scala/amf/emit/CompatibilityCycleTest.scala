package amf.emit

import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParser
import amf.core.client.scala.transform.AMFTransformer
import amf.core.internal.remote.{Hint, Oas20JsonHint, Oas30YamlHint, Raml10YamlHint}
import amf.core.internal.resource.StringResourceLoader
import amf.io.FileAssertionTest
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class CompatibilityCycleTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "amf-cli/shared/src/test/resources/compatibility/"

  test("RAML with union to OAS 2.0") {
    compatibility("raml10/raml-union-type.raml", "oas20/raml-union-type.json", Raml10YamlHint, Oas20JsonHint)
  }

  test("RAML with union to OAS 3.0") {
    compatibility("raml10/raml-union-type.raml", "oas30/raml-union-type.yaml", Raml10YamlHint, Oas30YamlHint)
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  private def compatibility(source: String, golden: String, from: Hint, to: Hint): Future[Assertion] = {
    for {
      input     <- fs.asyncFile(basePath + source).read()
      processed <- processInput(input.toString, from, to)
      output    <- Future.successful(new AMFRenderer(processed, to, RenderOptions()).renderToString)
      temp      <- writeTemporaryFile(golden)(output)
      result    <- assertDifferences(temp, basePath + golden)
    } yield {
      result
    }
  }

  private def processInput(content: String, from: Hint, to: Hint): Future[BaseUnit] = {
    val eh = UnhandledErrorHandler
    val fromConfig = APIConfiguration
      .fromSpec(from.spec)
      .withErrorHandlerProvider(() => eh)
      .withResourceLoader(StringResourceLoader("amf://id#", content, Some(from.spec.mediaType)))
    val toConfig = APIConfiguration
      .fromSpec(to.spec)
      .withErrorHandlerProvider(() => eh)
    for {
      unit     <- AMFParser.parse("amf://id#", fromConfig)
      resolved <- Future.successful(AMFTransformer.transform(unit.baseUnit, PipelineId.Compatibility, toConfig))
    } yield resolved.baseUnit
  }
}
