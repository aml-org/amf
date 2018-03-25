package amf.client.parse

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * [[amf.client.model.domain.Payload]] parser.
  */
@JSExportAll
class YamlPayloadParser extends Parser("AMF Payload", "application/amf+yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
