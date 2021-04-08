package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml10
import amf.plugins.document.webapi.Raml10Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Raml 1.0 parser.
  */
class Raml10Parser private (private val env: Option[Environment])
    extends Parser(Raml10.name, "application/yaml", env) {

  @JSExportTopLevel("Raml10Parser")
  def this() = this(None)
  @JSExportTopLevel("Raml10Parser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)

}
