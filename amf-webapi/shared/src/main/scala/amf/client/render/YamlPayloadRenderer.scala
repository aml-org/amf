package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Payload
import amf.plugins.document.webapi.PayloadPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("YamlPayloadRenderer")
class YamlPayloadRenderer extends Renderer(Payload.name, "application/payload+yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
}
