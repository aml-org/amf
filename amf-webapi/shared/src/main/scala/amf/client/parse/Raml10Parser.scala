package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml10
import amf.plugins.document.webapi.Raml10Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml parser.
  */
@JSExportAll
class Raml10Parser private (private val env: Option[Environment] = None)
    extends Parser(Raml10.name, "application/yaml", env) {

  @JSExportTopLevel("Raml10Parser")
  def this() = this(None)

  @JSExportTopLevel("Raml10Parser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
}
