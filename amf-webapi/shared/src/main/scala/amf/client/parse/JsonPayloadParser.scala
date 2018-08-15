package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Payload
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * [[amf.client.model.domain.Payload]] parser.
  */
@JSExportAll
class JsonPayloadParser private (private val env: Option[Environment] = None)
    extends Parser(Payload.name, "application/amf+json", env) {

  @JSExportTopLevel("JsonPayloadParser")
  def this() = this(None)

  @JSExportTopLevel("JsonPayloadParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
