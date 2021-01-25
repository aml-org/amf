package amf.client.`new`.builder

import amf.client.`new`.{AmfEnvironment, AmfInstance, AmfRegistry, AmfResult, ErrorHandlerProvider}
import amf.client.remote.Content
import amf.core.remote.Vendor
import amf.internal.resource.{ResourceLoader, StringResourceLoader}

import scala.concurrent.Future

class AmfTextParserInstanceBuilder extends AmfInstanceBuilder {

  case class InMemoryResourceLoader(var content: String) extends ResourceLoader {
    val uri = "file://amf-default-document.yaml"

    /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
    override def fetch(resource: String): Future[Content] = Future.successful(new Content(content, uri))

    /** Accepts specified resource. */
    override def accepts(resource: String): Boolean = resource == uri
  }

  private val memoryResourceLoader = InMemoryResourceLoader("")
  override def build() = {
    envBuilder
      .resolverBuilder()
      .withResourceLoader(memoryResourceLoader) // make sure that in memory is the first option
    new AmfTextParserInstance(envBuilder.build(), errorHandlerProvider, registryBuilder.build())

  }

  class AmfTextParserInstance(env: AmfEnvironment, errorHandlerProvider: ErrorHandlerProvider, registry: AmfRegistry)
      extends AmfInstance(env, errorHandlerProvider, registry) {

    override def parse(content: String, vendor: Option[Vendor]): Future[AmfResult] = {

      memoryResourceLoader.content = content
      super.parse(content, vendor)
    }
  }

}
