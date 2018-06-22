package amf.internal.resource

import amf.client.remote.Content

import scala.concurrent.Future

case class StringResourceLoader(url: String, content: String) extends ResourceLoader {
  private val _content = new Content(content, url)

  /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
  override def fetch(resource: String): Future[Content] = Future.successful(_content)

  /** Accepts specified resource. */
  override def accepts(resource: String): Boolean = resource == url
}
