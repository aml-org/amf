package amf.client.environment

import amf.core.unsafe.PlatformSecrets
import amf.client.convert.CoreClientConverters._
import amf.client.resource

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("client.DefaultEnvironment")
object DefaultEnvironment extends PlatformSecrets {
  @JSExport("apply")
  def apply(): Environment = {
    val loaders: ClientList[ClientLoader] = platform.loaders().asClient.asInstanceOf[ClientList[ClientLoader]]
    Environment.empty().withLoaders(loaders)
  }
}
