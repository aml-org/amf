package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("YamlPayloadRenderer")
class YamlPayloadRenderer extends Renderer("YAML Payload", "application/payload+yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
