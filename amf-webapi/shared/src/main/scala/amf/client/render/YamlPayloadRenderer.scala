package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Payload
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * YAML payload generator.
  */
class YamlPayloadRenderer private (private val env: Option[Environment])
    extends Renderer(Payload.name, "application/payload+yaml", env) {

  @JSExportTopLevel("YamlPayloadRenderer")
  def this() = this(None)
  @JSExportTopLevel("YamlPayloadRenderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
