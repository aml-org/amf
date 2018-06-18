package amf.internal.resource

import amf.client.remote.Content
import amf.client.resource.{ResourceLoader => ClientResourceLoader}
import amf.client.convert.CoreClientConverters._

import scala.concurrent.Future

/** Adapts a client ResourceLoader to an internal one. */
case class ResourceLoaderAdapter(private[amf] val adaptee: ClientResourceLoader) extends ResourceLoader {

  override def fetch(resource: String): Future[Content] = adaptee.fetch(resource).asInternal

  override def accepts(resource: String): Boolean = adaptee.accepts(resource)
}
