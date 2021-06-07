package amf.emit

import amf.ProfileName
import amf.client.environment.WebAPIConfiguration
import amf.client.errorhandling.DefaultErrorHandler
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.AMFCompiler
import amf.client.remod.{AMFParser, AMFValidator}
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.services.RuntimeValidator
import amf.facades.Validation
import amf.internal.resource.StringResourceLoader
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class CompatibilityTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "amf-cli/shared/src/test/resources/upanddown/"

  test("leagues raml to oas") {
    compatibility("leagues-api.raml", Raml10YamlHint, Oas20JsonHint)
  }

  test("leagues oas to raml") {

    compatibility("leagues-api.json", Oas20JsonHint, Raml10YamlHint)
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  private def compatibility(source: String, l: Hint, r: Hint): Future[Assertion] = {
    for {
      input  <- fs.asyncFile(basePath + source).read()
      left   <- parseBaseUnit(input.toString, l)
      target <- Future.successful(new AMFRenderer(left, r.vendor, RenderOptions(), Some(r.syntax)).renderToString)
      _      <- parseBaseUnit(target, r)
    } yield {
      succeed
    }
  }

  private def parseBaseUnit(content: String, hint: Hint): Future[BaseUnit] = {
    val eh = DefaultErrorHandler()
    val conf = WebAPIConfiguration
      .WebAPI()
      .withErrorHandlerProvider(() => eh)
      .withResourceLoader(StringResourceLoader("amf://id#", content))
    for {
      unit <- AMFParser.parse("amf://id#", conf)
      _    <- AMFValidator.validate(unit.bu, ProfileName(hint.vendor.name), conf)
    } yield unit.bu
  }
}
