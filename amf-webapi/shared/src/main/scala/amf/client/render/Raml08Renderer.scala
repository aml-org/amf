package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml08
import amf.plugins.document.webapi.Raml08Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Raml 0.8 generator.
  */
class Raml08Renderer private (private val env: Option[Environment])
    extends Renderer(Raml08.name, "application/yaml", env) {

  @JSExportTopLevel("Raml08Renderer")
  def this() = this(None)
  @JSExportTopLevel("Raml08Renderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
}
