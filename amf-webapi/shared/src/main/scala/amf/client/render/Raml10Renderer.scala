package amf.client.render

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml10
import amf.plugins.document.webapi.Raml10Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Raml 1.0 generator.
  */
class Raml10Renderer private (private val env: Option[Environment])
    extends Renderer(Raml10.name, "application/yaml", env) {

  @JSExportTopLevel("Raml10Renderer")
  def this() = this(None)
  @JSExportTopLevel("Raml10Renderer")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
}