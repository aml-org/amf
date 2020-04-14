package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas20
import amf.plugins.document.webapi.Oas20Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Oas 2.0 generator.
  */
class Oas20Renderer private (private val env: Option[Environment])
    extends Renderer(Oas20.name, "application/json", env) {

  @JSExportTopLevel("Oas20Renderer")
  def this() = this(None)
  @JSExportTopLevel("Oas20Renderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
}
