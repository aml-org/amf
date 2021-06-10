package amf.compiler

import amf.{ProfileName, RAMLStyle, Raml10Profile}
import amf.client.parse.DefaultParserErrorHandler
import amf.client.remote.Content
import amf.core.remote.{Cache, Context, FileNotFound, Raml10}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.{RuntimeCompiler, RuntimeResolver, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ApikitApiSyncCasesTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers{

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override protected def beforeEach(): Future[Unit] = {
    Validation.apply(platform).map(_.init())
  }

  test("Resource loader shouldn't have absolute path if ref and base aren't absolute") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:main.raml" -> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/main.raml",
      "something.raml"-> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/something.raml",
      "examples/something/get-something-response.raml"-> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/get-something-response.raml",
      "examples/something/put-something-request.raml"-> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/put-something-request.raml",
      "examples/common/async-response.raml"-> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/common/async-response.raml",
      "libraries/resourceTypes.raml" -> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/libraries/resourceTypes.raml",
    )
    val url = "resource::really-cool-urn:1.0.0:raml:zip:main.raml"
    val errorHandler = DefaultParserErrorHandler()
    val loaders = Seq(new URNResourceLoader(mappings))
    val env = Environment().withLoaders(loaders)
    RuntimeCompiler.apply(url, None, None, base = Context(platform), cache = Cache(), errorHandler = errorHandler, env = env).map { unit =>
      errorHandler.getErrors should have size 0
    }
  }

  test("Parsing context generated in extends resolution stage for raml traits") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml" -> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/townfile.raml",
      "some-modules/for-health-check.raml" -> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/some-modules/for-health-check.raml",
      "some-modules/trait.raml" -> "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/some-modules/trait.raml",
    )
    val url = "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml"
    val loaders = Seq(new URNResourceLoader(mappings))
    val env = Environment().withLoaders(loaders)
    for {
     unit <- RuntimeCompiler.apply(url, None, None, base = Context(platform), cache = Cache(), env = env)
    _ <- Future.successful(RuntimeResolver.resolve(Raml10.name, unit, ResolutionPipeline.EDITING_PIPELINE))
    report <- RuntimeValidator(unit, Raml10Profile, RAMLStyle, resolved = true)
    }yield {
      report.results should have size 0
    }
  }

  class URNResourceLoader(mappings: Map[String, String]) extends ResourceLoader {

    override def fetch(resource: String): Future[Content] = {
      mappings.get(resource)
        .map(platform.resolve)
        .getOrElse(throw FileNotFound(new RuntimeException(s"Couldn't find resource $resource")))
    }

    override def accepts(resource: String): Boolean = true
  }
}
