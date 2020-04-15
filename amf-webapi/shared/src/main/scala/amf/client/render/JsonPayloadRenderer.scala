package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Payload
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * JSON payload generator.
  */
class JsonPayloadRenderer private (private val env: Option[Environment])
    extends Renderer(Payload.name, "application/payload+json", env) {

  @JSExportTopLevel("JsonPayloadRenderer")
  def this() = this(None)
  @JSExportTopLevel("JsonPayloadRenderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
