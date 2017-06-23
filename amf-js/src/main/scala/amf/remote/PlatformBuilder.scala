package amf.remote

import amf.Utils.isClient
import amf.remote.browser.JsBrowserPlatform
import amf.remote.server.JsServerPlatform

object PlatformBuilder {
  def apply(): Platform = if (isClient) new JsBrowserPlatform() else new JsServerPlatform()
}
