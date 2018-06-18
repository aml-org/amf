package amf.core.unsafe

import amf.core.remote.{JsPlatform, Platform}
import amf.core.remote.browser.JsBrowserPlatform
import amf.core.remote.server.JsServerPlatform

import scala.scalajs.js.{Dynamic, isUndefined}

object PlatformBuilder {

  val platform: JsPlatform = if (isBrowser) new JsBrowserPlatform() else new JsServerPlatform()

  def apply(): JsPlatform = platform

  /** Return true if js is running on browser. */
  private def isBrowser: Boolean = !isUndefined(Dynamic.global.document)
}
