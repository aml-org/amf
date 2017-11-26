package amf.core.unsafe

import amf.core.remote.JvmPlatform


object PlatformBuilder {
  val platform = new JvmPlatform()
  def apply(): JvmPlatform = platform
}