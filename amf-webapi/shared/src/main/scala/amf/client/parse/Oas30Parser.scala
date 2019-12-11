package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas30
import amf.plugins.document.webapi.Oas30Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Oas parser.
  */
@JSExportAll
class Oas30Parser private (private val env: Option[Environment] = None)
    extends Parser(Oas30.name, "application/json", env) {

  @JSExportTopLevel("Oas30Parser")
  def this() = this(None)

  @JSExportTopLevel("Oas30Parser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
}
