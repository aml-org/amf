package amf.client.resource

import amf.client.convert.CoreClientConverters._
import amf.client.remote.Content

trait ResourceLoader {

  /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
  def fetch(resource: String): ClientFuture[Content]

  /** Accepts specified resource. */
  def accepts(resource: String): Boolean = true
}
