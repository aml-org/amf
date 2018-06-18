package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.vocabularies.AMLPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("Aml10Renderer")
class Aml10Renderer(private val mediaType: String)
  extends Renderer("AML 1.0", mediaType) {

  @JSExportTopLevel("Aml10Renderer")
  def this() = this("application/yaml")

  AMFPluginsRegistry.registerDocumentPlugin(AMLPlugin)
}
