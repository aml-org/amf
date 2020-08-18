package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas30
import amf.plugins.document.webapi.Oas30Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Oas 3.0 generator.
  */
class Oas30Renderer private (private val env: Option[Environment])
    extends Renderer(Oas30.name, "application/json", env) {

  override def defaultShapeRenderOptions(): ShapeRenderOptions = ShapeRenderOptions().withCompactedEmission

  @JSExportTopLevel("Oas30Renderer")
  def this() = this(None)
  @JSExportTopLevel("Oas30Renderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
}