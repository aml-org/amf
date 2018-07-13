package amf.core.remote

import java.net.URI

import amf.core.remote.server.Path

import scala.scalajs.js.URIUtils

trait JsPlatform extends Platform {

  override  def name = "js"

  override def findCharInCharSequence(stream: CharSequence)(p: Char => Boolean): Option[Char] = stream.toString.find(p)

  /** encodes a complete uri. Not encodes chars like / */
  override def encodeURI(url: String): String = URIUtils.encodeURI(url)

  /** encodes a uri component, including chars like / and : */
  override def encodeURIComponent(url: String): String = URIUtils.encodeURIComponent(url)

  /** decode a complete uri. */
  override def decodeURI(url: String): String = URIUtils.decodeURI(url)

  /** decodes a uri component */
  override def decodeURIComponent(url: String): String = URIUtils.decodeURIComponent(url)

  override def normalizeURL(url: String): String = Path.normalize(url)

  override def normalizePath(url: String): String = new URI(encodeURI(url)).normalize.toString
}
