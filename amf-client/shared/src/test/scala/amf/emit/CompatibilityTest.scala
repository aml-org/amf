package amf.emit
import amf.ProfileName
import amf.core.emitter.RenderOptions
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.facades.Validation
import amf.internal.environment.Environment
import amf.internal.resource.StringResourceLoader
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class CompatibilityTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "amf-client/shared/src/test/resources/upanddown/"

  test("leagues raml to oas") {
    compatibility("leagues-api.raml", RamlYamlHint, OasJsonHint)
  }

  test("leagues oas to raml") {

    compatibility("leagues-api.json", OasJsonHint, RamlYamlHint)
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
      case RamlYamlHint | OasYamlHint => "application/yaml"
      case _                          => "application/json"
    }

    val environment = AMFPluginsRegistry.obtainStaticEnv()

    for {
      unit <- RuntimeCompiler(
        "amf://id#",
        Some(mediaType),
        Some(hint.vendor.name),
        Context(platform),
        env = Environment(StringResourceLoader("amf://id#", content)),
        cache = Cache(),
        newEnv = environment
      )
      _ <- RuntimeValidator(unit, ProfileName(hint.vendor.name))
    } yield unit
  }
}
