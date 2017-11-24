package amf.framework.unsafe

import amf.remote.JvmPlatform


object PlatformBuilder {
  val platform = new JvmPlatform()
  def apply(): JvmPlatform = platform
}