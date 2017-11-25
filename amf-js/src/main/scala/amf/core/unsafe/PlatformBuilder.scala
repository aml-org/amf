package amf.core.unsafe

import amf.Utils.isClient
import amf.core.remote.Platform
import amf.remote.browser.JsBrowserPlatform
import amf.remote.server.JsServerPlatform

object PlatformBuilder {
  val platform = if (isClient) new JsBrowserPlatform() else new JsServerPlatform()
  def apply(): Platform = platform
}
