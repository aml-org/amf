package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.RAML08Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml parser.
  */
@JSExportAll
class Raml08Parser private (private val env: Option[Environment] = None)
    extends Parser("RAML 0.8", "application/yaml", env) {

  @JSExportTopLevel("Raml08Parser")
  def this() = this(None)

  @JSExportTopLevel("Raml08Parser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
