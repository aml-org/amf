package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.OAS20Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Oas parser.
  */
@JSExportAll
class Oas20Parser private (private val env: Option[Environment] = None)
    extends Parser("OAS 2.0", "application/json", env) {

  @JSExportTopLevel("Oas20Parser")
  def this() = this(None)

  @JSExportTopLevel("Oas20Parser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
}
