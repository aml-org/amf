package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.{RAML08Plugin, RAML10Plugin}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml parser.
  */
@JSExportAll
class RamlParser private (private val env: Option[Environment] = None)
    extends Parser("RAML", "application/yaml", env) {

  @JSExportTopLevel("RamlParser")
  def this() = this(None)

  @JSExportTopLevel("RamlParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
