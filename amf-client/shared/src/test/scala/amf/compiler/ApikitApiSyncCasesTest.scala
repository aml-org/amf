package amf.compiler

import amf.client.parse.DefaultParserErrorHandler
import amf.client.remote.Content
import amf.core.remote.{Cache, Context, FileNotFound, Raml10}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.{RuntimeCompiler, RuntimeResolver, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import amf.{RAMLStyle, Raml10Profile}
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ApikitApiSyncCasesTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override protected def beforeEach(): Future[Unit] = {
    Validation.apply(platform).map(_.init())
  }

  test("Resource loader shouldn't have absolute path if ref and base aren't absolute") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:main.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/main.raml"),
      "something.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/something.raml"),
      "examples/something/get-something-response.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/get-something-response.raml"),
      "examples/something/put-something-request.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/put-something-request.raml"),
      "examples/common/async-response.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/common/async-response.raml"),
      "libraries/resourceTypes.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/libraries/resourceTypes.raml"),
    )
    val url          = "resource::really-cool-urn:1.0.0:raml:zip:main.raml"
    val errorHandler = DefaultParserErrorHandler()
    val loaders      = Seq(new URNResourceLoader(mappings))
    val env          = Environment().withLoaders(loaders)
    RuntimeCompiler
      .apply(url, None, None, base = Context(platform), cache = Cache(), errorHandler = errorHandler, env = env)
      .map { unit =>
        errorHandler.getErrors should have size 0
      }
  }

  // APIMF-3118
  test("Resource loader should find nested references") {
    val mappings = Map(
      "resource::37fab092-be99-4538-b5ce-b004c5439f6d:refexample:1.0.1:oas:zip:example.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/example.json"),
      "exchange.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/exchange.json"),
      "components/schemas/okResponse.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/components/schemas/okResponse.json"),
      "components/schemas/item.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/components/schemas/item.json"),
      "components/schemas/nested.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/components/schemas/nested.json"),
    )
    val url          = "resource::37fab092-be99-4538-b5ce-b004c5439f6d:refexample:1.0.1:oas:zip:example.json"
    val errorHandler = DefaultParserErrorHandler()
    val loaders      = Seq(new URNResourceLoader(mappings))
    val env          = Environment().withLoaders(loaders)
    RuntimeCompiler
      .apply(url, None, None, base = Context(platform), cache = Cache(), errorHandler = errorHandler, env = env)
      .map { unit =>
        errorHandler.getErrors should have size 0
      }
  }

  test("Parsing context generated in extends resolution stage for raml traits") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/townfile.raml"),
      "some-modules/for-health-check.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/some-modules/for-health-check.raml"),
      "some-modules/trait.raml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/some-modules/trait.raml"),
    )
    val url     = "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml"
    val loaders = Seq(new URNResourceLoader(mappings))
    val env     = Environment().withLoaders(loaders)
    for {
      unit   <- RuntimeCompiler.apply(url, None, None, base = Context(platform), cache = Cache(), env = env)
      _      <- Future.successful(RuntimeResolver.resolve(Raml10.name, unit, ResolutionPipeline.EDITING_PIPELINE))
      report <- RuntimeValidator(unit, Raml10Profile, RAMLStyle, resolved = true)
    } yield {
      report.results should have size 0
    }
  }

  // APIMF-3384
  test("should accept content URLs starting with 'jar:'") {
    val mappings = Map(
      "resource::com.mycompany:consumer-api:1.0.0:oas:zip:consumer.yaml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/jar-protocol/consumer.yaml"
      ),
      "utility.yaml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/jar-protocol/utility.yaml"
      ),
    )
    val url = "resource::com.mycompany:consumer-api:1.0.0:oas:zip:consumer.yaml"
    val loaders = Seq(new URNResourceLoader(mappings))
    val env     = Environment().withLoaders(loaders)
    for {
      unit   <- RuntimeCompiler.apply(url, None, None, base = Context(platform), cache = Cache(), env = env)
      _      <- Future.successful(RuntimeResolver.resolve(Raml10.name, unit, ResolutionPipeline.EDITING_PIPELINE))
      report <- RuntimeValidator(unit, Raml10Profile, RAMLStyle, resolved = true)
    } yield {
      report.results should have size 0
    }
  }

  // APIMF-3600
  test("exchange modules with multiple zip sources") {
    val mappings = Map(
      "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:se-23375:1.0.1:oas:zip:se-23375.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/exchange-modules/se-23375.json"
      ),
      "exchange_modules/8fe5354c-e64c-4eaa-addc-b50906a0b48c/datamodel-tmforum/1.0.0/4.1.0/Customer/Bucket.schema.json" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/exchange-modules/Bucket.schema.json"
      ),
    )
    val url = "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:se-23375:1.0.1:oas:zip:se-23375.json"
    val errorHandler = DefaultParserErrorHandler()
    val loaders      = Seq(new URNResourceLoader(mappings))
    val env          = Environment().withLoaders(loaders)
    RuntimeCompiler
      .apply(url, None, None, base = Context(platform), cache = Cache(), errorHandler = errorHandler, env = env)
      .map { unit =>
        errorHandler.getErrors should have size 0
      }
  }

  // APIMF-3533
  test("references to external yaml using './'") {
    val mappings = Map(
      "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:pure-member:1.0.0:oas:zip:pure-member-portal.yaml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/dot-slash-ref/pure-member-portal.yaml"
      ),
      "responses.yaml" -> CustomContentResult(
        "file://amf-client/shared/src/test/resources/compiler/apikit-apisync/dot-slash-ref/responses.yaml"
      ),
    )
    val url = "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:pure-member:1.0.0:oas:zip:pure-member-portal.yaml"
    val errorHandler = DefaultParserErrorHandler()
    val loaders      = Seq(new URNResourceLoader(mappings))
    val env          = Environment().withLoaders(loaders)
    RuntimeCompiler
      .apply(url, None, None, base = Context(platform), cache = Cache(), errorHandler = errorHandler, env = env)
      .map { unit =>
        errorHandler.getErrors should have size 0
      }
  }

  case class CustomContentResult(actualPath: String)

  class URNResourceLoader(mappings: Map[String, CustomContentResult]) extends ResourceLoader {

    override def fetch(resource: String): Future[Content] = {
      mappings
        .get(resource)
        .map(url => {
          platform
            .resolve(url.actualPath)
            .map(content => new Content(content.stream, resource))
        })
        .getOrElse(throw FileNotFound(new RuntimeException(s"Couldn't find resource $resource")))
    }

    override def accepts(resource: String): Boolean = true
  }
}
