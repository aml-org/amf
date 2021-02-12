package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml08
import amf.plugins.document.webapi.{ExternalJsonYamlRefsParsePlugin, Oas20ParsePlugin, Raml08ParsePlugin, Raml08Plugin}

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Raml 0.8 parser.
  */
class Raml08Parser private (private val env: Option[Environment])
    extends Parser(Raml08.name, "application/yaml", env) {

  @JSExportTopLevel("Raml08Parser")
  def this() = this(None)
  @JSExportTopLevel("Raml08Parser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(Raml08ParsePlugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(ExternalJsonYamlRefsParsePlugin)
}
