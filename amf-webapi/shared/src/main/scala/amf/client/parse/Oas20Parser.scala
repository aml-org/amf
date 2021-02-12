package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas20
import amf.plugins.document.webapi.{Async20ParsePlugin, ExternalJsonYamlRefsParsePlugin, Oas20ParsePlugin, Oas20Plugin}

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Oas 2.0 JSON parser.
  */
class Oas20Parser private (private val env: Option[Environment]) extends Parser(Oas20.name, "application/json", env) {

  @JSExportTopLevel("Oas20Parser")
  def this() = this(None)
  @JSExportTopLevel("Oas20Parser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(Oas20ParsePlugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(ExternalJsonYamlRefsParsePlugin)

}
