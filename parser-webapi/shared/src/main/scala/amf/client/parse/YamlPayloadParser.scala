package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * [[amf.client.model.domain.Payload]] parser.
  */
@JSExportAll
class YamlPayloadParser private (private val env: Option[Environment] = None)
    extends Parser("AMF Payload", "application/amf+yaml", env) {

  @JSExportTopLevel("YamlPayloadParser")
  def this() = this(None)

  @JSExportTopLevel("YamlPayloadParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
