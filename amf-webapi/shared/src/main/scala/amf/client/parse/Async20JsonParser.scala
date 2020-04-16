package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.AsyncApi20
import amf.plugins.document.webapi.Async20Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Async 2.0 parser.
  */
@JSExportAll
class Async20JsonParser private (private val env: Option[Environment] = None)
    extends Parser(AsyncApi20.name, "application/json", env) {

  @JSExportTopLevel("Async20JsonParser")
  def this() = this(None)

  @JSExportTopLevel("Async20JsonParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
}
