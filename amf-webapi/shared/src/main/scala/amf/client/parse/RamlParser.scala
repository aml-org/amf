package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.{Raml08Plugin, Raml10Plugin}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml parser.
  */
@JSExportAll
class RamlParser private (private val env: Option[Environment] = None)
    extends Parser(Raml.name, "application/yaml", env) {

  @JSExportTopLevel("RamlParser")
  def this() = this(None)

  @JSExportTopLevel("RamlParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
  AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
}
