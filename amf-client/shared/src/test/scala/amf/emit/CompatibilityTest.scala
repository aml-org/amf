package amf.emit
import amf.ProfileName
import amf.client.environment.WebAPIConfiguration
import amf.client.parse.DefaultErrorHandler
import amf.client.remod.ParseConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.emitter.RenderOptions
import amf.core.remote._
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.facades.Validation
import amf.internal.resource.StringResourceLoader
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class CompatibilityTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "amf-client/shared/src/test/resources/upanddown/"

  test("leagues raml to oas") {
    compatibility("leagues-api.raml", Raml10YamlHint, Oas20JsonHint)
  }

  test("leagues oas to raml") {

    compatibility("leagues-api.json", Oas20JsonHint, Raml10YamlHint)
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  private def compatibility(source: String, l: Hint, r: Hint): Future[Assertion] = {
    for {
      _      <- Validation(platform)
      input  <- fs.asyncFile(basePath + source).read()
      left   <- parseBaseUnit(input.toString, l)
      target <- new AMFRenderer(left, r.vendor, RenderOptions(), Some(r.syntax)).renderToString
      _      <- parseBaseUnit(target, r)
    } yield {
      succeed
    }
  }

  private def parseBaseUnit(content: String, hint: Hint) = {
    val mediaType: String = hint match {
      case Raml10YamlHint | Oas20YamlHint => "application/yaml"
      case _                              => "application/json"
    }
    val eh = DefaultErrorHandler()
    val conf = WebAPIConfiguration
      .WebAPI()
      .withErrorHandlerProvider(() => eh)
      .withResourceLoader(StringResourceLoader("amf://id#", content))
    for {
      unit <- RuntimeCompiler(
        Some(hint.vendor.mediaType),
        Context(platform),
        cache = Cache(),
        new ParseConfiguration(conf, "amf://id#")
      )
      _ <- RuntimeValidator(unit, ProfileName(hint.vendor.name), resolved = false, new ValidationConfiguration(conf))
    } yield unit
  }
}
