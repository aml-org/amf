package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Amf
import amf.plugins.document.graph.AMFGraphPlugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * JSON-LD generator.
  */
class JsonldRenderer private (private val env: Option[Environment])
    extends Renderer(Amf.name, "application/ld+json", env) {

  @JSExportTopLevel("JsonldRenderer")
  def this() = this(None)
  @JSExportTopLevel("JsonldRenderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
}
