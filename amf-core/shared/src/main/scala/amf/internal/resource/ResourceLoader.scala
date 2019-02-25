package amf.internal.resource

import amf.client.remote.Content

import scala.concurrent.Future

trait ResourceLoader {

  /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
  def fetch(resource: String): Future[Content]

  /** Accepts specified resource. */
  def accepts(resource: String): Boolean
}