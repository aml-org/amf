package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas20
import amf.plugins.document.webapi.Oas20Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Oas 2.0 YAML parser.
  */
class Oas20YamlParser private (private val env: Option[Environment])
    extends Parser(Oas20.name, "application/yaml", env) {

  @JSExportTopLevel("Oas20YamlParser")
  def this() = this(None)
  @JSExportTopLevel("Oas20YamlParser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
}
