package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.{
  ExternalJsonYamlRefsParsePlugin,
  Raml08ParsePlugin,
  Raml08Plugin,
  Raml10ParsePlugin,
  Raml10Plugin
}

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Raml parser.
  */
class RamlParser private (private val env: Option[Environment] = None)
    extends Parser(Raml.name, "application/yaml", env) {

  @JSExportTopLevel("RamlParser")
  def this() = this(None)
  @JSExportTopLevel("RamlParser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
  AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(Raml10ParsePlugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(Raml08ParsePlugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(ExternalJsonYamlRefsParsePlugin)
}
