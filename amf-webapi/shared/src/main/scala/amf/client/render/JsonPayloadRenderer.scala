package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("JsonPayloadRenderer")
class JsonPayloadRenderer extends Renderer("JSON Payload", "application/payload+json") {
  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
