package amf.client.remote

import amf.core.lexer.{CharSequenceStream, CharStream}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Content(stream: CharStream, url: String, mime: Option[String] = None) {

  @JSExportTopLevel("client.remote.Content")
  def this(stream: String, url: String) = this(new CharSequenceStream(url, stream), url)

  @JSExportTopLevel("client.remote.Content")
  def this(stream: String, url: String, mime: String) = this(new CharSequenceStream(url, stream), url, Some(mime))

  def this(stream: String, url: String, mime: Option[String]) = this(new CharSequenceStream(url, stream), url, mime)
}
