package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas30
import amf.plugins.document.webapi.Oas30Plugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Oas 3.0 YAML parser.
  */
class Oas30YamlParser private (private val env: Option[Environment])
    extends Parser(Oas30.name, "application/yaml", env) {

  @JSExportTopLevel("Oas30YamlParser")
  def this() = this(None)
  @JSExportTopLevel("Oas30YamlParser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
}