package amf.client.parse

import amf.client.environment.Environment
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Amf parser.
  */
@JSExportAll
class AmfGraphParser private (private val env: Option[Environment] = None)
    extends Parser("AMF Graph", "application/ld+json", env) {

  @JSExportTopLevel("AmfGraphParser")
  def this() = this(None)

  @JSExportTopLevel("AmfGraphParser")
  def this(env: Environment) = this(Some(env))

  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
}
