package amf.client.parse

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * [[amf.client.model.domain.Payload]] parser.
  */
@JSExportAll
class JsonPayloadParser extends Parser("AMF Payload", "application/amf+json") {
  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
