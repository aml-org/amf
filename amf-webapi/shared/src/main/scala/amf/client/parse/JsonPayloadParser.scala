package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Payload
import amf.plugins.document.webapi.{
  Async20ParsePlugin,
  ExternalJsonYamlRefsParsePlugin,
  PayloadParsePlugin,
  PayloadPlugin
}

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[amf.client.model.domain.Payload]] parser.
  */
class JsonPayloadParser private (private val env: Option[Environment])
    extends Parser(Payload.name, "application/amf+json", env) {

  @JSExportTopLevel("JsonPayloadParser")
  def this() = this(None)
  @JSExportTopLevel("JsonPayloadParser")
  def this(environment: Environment) = this(Some(environment))

  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(PayloadParsePlugin)
  AMFPluginsRegistry.registerNewInterfacePlugin(ExternalJsonYamlRefsParsePlugin)

}
