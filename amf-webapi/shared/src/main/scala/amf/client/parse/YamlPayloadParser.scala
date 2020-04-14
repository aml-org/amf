package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Payload
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[amf.client.model.domain.Payload]] parser.
  */
class YamlPayloadParser private (private val env: Option[Environment])
    extends Parser(Payload.name, "application/amf+yaml", env) {

  @JSExportTopLevel("YamlPayloadParser")
  def this() = this(None)
  @JSExportTopLevel("YamlPayloadParser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
