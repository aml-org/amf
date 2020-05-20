package amf.core

import amf.client.parse.DefaultParserErrorHandler
import amf.client.remote.Content
import amf.core.client.ParsingOptions
import amf.core.remote.{Cache, Context, Raml10}
import amf.core.services.RuntimeCompiler
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import amf.plugins.document.webapi.Raml10Plugin
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Future

class LocationInvariantTest extends AsyncFunSuite with PlatformSecrets with Matchers {

  test("Parsed root is invariant to the location used by the ResourceLoader") {
    val RESOURCE_LOADER_URL = "theContentsUrl"
    val ROOT_DOCUMENT_URL   = "file://heRealUrl"
    val content =
      """
        |#%RAML 1.0
        |title: anApi
        |""".stripMargin
    AMF
      .init()
      .flatMap { _ =>
        AMF.registerPlugin(Raml10Plugin)
        val env = Environment().withLoaders(Seq[ResourceLoader](new MockResourceLoader(content, RESOURCE_LOADER_URL)))
        RuntimeCompiler(
          ROOT_DOCUMENT_URL,
          Option("application/yaml"),
          Some(Raml10.name),
          Context(platform),
          env = env,
          cache = Cache(),
          parsingOptions = ParsingOptions(),
          errorHandler = DefaultParserErrorHandler.withRun()
        )
      }
      .flatMap { bu =>
        bu.location().map(x => x shouldEqual ROOT_DOCUMENT_URL).getOrElse(fail())
      }
  }

  class MockResourceLoader(content: String, url: String) extends ResourceLoader {

    /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
    override def fetch(resource: String): Future[Content] = Future { new Content(content, url, "application/yaml") }

    /** Accepts specified resource. */
    override def accepts(resource: String): Boolean = true
  }
}
