package amf.unsafe

import amf.remote._

trait PlatformSecrets {
  val platform: Platform = PlatformBuilder()
}
