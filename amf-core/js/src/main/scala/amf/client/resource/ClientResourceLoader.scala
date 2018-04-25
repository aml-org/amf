package amf.client.resource

import amf.client.convert.CoreClientConverters.ClientFuture
import amf.client.remote.Content

import scala.scalajs.js

@js.native
trait ClientResourceLoader extends js.Object {

  /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
  def fetch(resource: String): ClientFuture[Content] = js.native

  /** Accepts specified resource. */
  def accepts(resource: String): Boolean = js.native
}
