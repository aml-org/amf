package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.vocabularies.AMLPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Aml10Parser private (private val mediaType: String, private val env: Option[Environment] = None)
  extends Parser("AML 1.0", mediaType, env) {

  @JSExportTopLevel("Aml10Parser")
  def this() = this("application/yaml", None)

  @JSExportTopLevel("Aml10Parser")
  def this(mediaType: String) = this(mediaType, None)

  @JSExportTopLevel("Aml10Parser")
  def this(mediaType: String, env: Environment) = this(mediaType, Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(AMLPlugin)
}
