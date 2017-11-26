package amf.core.unsafe

import amf.core.Utils._
import amf.core.remote.Platform
import amf.core.remote.browser.JsBrowserPlatform
import amf.core.remote.server.JsServerPlatform

object PlatformBuilder {
  val platform: Platform = if (isClient) new JsBrowserPlatform() else new JsServerPlatform()
  def apply(): Platform = platform
}
