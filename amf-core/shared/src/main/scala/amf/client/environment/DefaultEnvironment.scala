package amf.client.environment

import amf.core.unsafe.PlatformSecrets
import amf.client.convert.CoreClientConverters._
import amf.core.remote.Platform

object DefaultEnvironment extends PlatformSecrets {
  def apply(): Environment = Environment.empty().withLoaders(platform.loaders().asClient)
}
