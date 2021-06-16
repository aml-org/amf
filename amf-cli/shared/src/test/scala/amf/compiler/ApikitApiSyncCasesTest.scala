package amf.compiler

import amf.apicontract.client.scala.config.WebAPIConfiguration
import amf.core.client.common.remote.Content
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.internal.parser.{AMFCompiler, ParseConfiguration}
import amf.core.internal.remote.{Cache, Context, FileNotFound}
import amf.core.internal.resource.ResourceLoader
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ApikitApiSyncCasesTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Resource loader shouldn't have absolute path if ref and base aren't absolute") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:main.raml" -> "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/main.raml",
      "something.raml"                                     -> "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/something.raml",
      "examples/something/get-something-response.raml"     -> "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/get-something-response.raml",
      "examples/something/put-something-request.raml"      -> "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/put-something-request.raml",
      "examples/common/async-response.raml"                -> "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/common/async-response.raml",
      "libraries/resourceTypes.raml"                       -> "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/libraries/resourceTypes.raml",
    )
    val url = "resource::really-cool-urn:1.0.0:raml:zip:main.raml"
    val eh  = DefaultErrorHandler()
    AMFCompiler(
        url,
        None,
        base = Context(platform),
        cache = Cache(),
        ParseConfiguration(
          WebAPIConfiguration
            .WebAPI()
            .withResourceLoaders(List(new URNResourceLoader(mappings)))
            .withErrorHandlerProvider(() => eh))
      ).build()
      .map { _ =>
        eh.getResults should have size 0
      }
  }

  class URNResourceLoader(mappings: Map[String, String]) extends ResourceLoader {

    override def fetch(resource: String): Future[Content] = {
      mappings
        .get(resource)
        .map(url => platform.fetchContent(url, AMFGraphConfiguration.predefined()))
        .getOrElse(throw FileNotFound(new RuntimeException(s"Couldn't find resource $resource")))
    }

    override def accepts(resource: String): Boolean = true
  }
}
