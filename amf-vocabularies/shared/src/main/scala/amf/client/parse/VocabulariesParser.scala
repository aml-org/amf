package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.vocabularies.AMLPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class VocabulariesParser private (private val env: Option[Environment] = None)
  extends Parser("AMF", "application/yaml", env) {

  @JSExportTopLevel("VocabulariesParser")
  def this() = this(None)

  @JSExportTopLevel("VocabulariesParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(AMLPlugin)
}
