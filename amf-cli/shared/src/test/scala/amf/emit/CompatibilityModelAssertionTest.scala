package amf.emit

import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParser
import amf.core.client.scala.transform.AMFTransformer
import amf.core.internal.remote.{Hint, Oas30YamlHint, Raml10YamlHint}
import amf.core.internal.resource.StringResourceLoader
import amf.core.io.FileAssertionTest

import scala.concurrent.Future

class CompatibilityModelAssertionTest extends FileAssertionTest {

  private val basePath = "amf-cli/shared/src/test/resources/compatibility/"

  test("Test no empty request in RAML to OAS conversion 1") {
    compatibility("raml10/empty-body-1.raml", Raml10YamlHint, Oas30YamlHint) map {
      output =>
        assert(output.nonEmpty)
        assert(!output.contains("content: {}"))
    }
  }

  test("Test no empty request in RAML to OAS conversion 2") {
    compatibility("raml10/empty-body-2.raml", Raml10YamlHint, Oas30YamlHint) map {
      output =>
        assert(output.nonEmpty)
        assert(!output.contains("content: {}"))
    }
  }

  /** Compile source with specified hint. Return render of target. */
  private def compatibility(source: String, from: Hint, to: Hint): Future[String] = {
    for {
      input     <- fs.asyncFile(basePath + source).read()
      processed <- processInput(input.toString, from, to)
      output    <- Future.successful(new AMFRenderer(processed, to, RenderOptions()).renderToString)
    } yield {
      output
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
