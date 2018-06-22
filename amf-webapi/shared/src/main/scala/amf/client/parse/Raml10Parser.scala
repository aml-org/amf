package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.RAML10Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml parser.
  */
@JSExportAll
class Raml10Parser private (private val env: Option[Environment] = None)
    extends Parser("RAML 1.0", "application/yaml", env) {

  @JSExportTopLevel("Raml10Parser")
  def this() = this(None)

  @JSExportTopLevel("Raml10Parser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
}
