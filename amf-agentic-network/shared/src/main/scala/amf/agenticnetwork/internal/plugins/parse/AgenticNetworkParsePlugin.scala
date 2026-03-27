package amf.agenticnetwork.internal.plugins.parse

import amf.agenticnetwork.internal.plugins.parse.entry.AgenticNetworkIdEntry
import amf.agenticnetwork.internal.plugins.parse.schema.AgenticNetworkSchemaLoader
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.ReferenceHandler
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgenticNetwork, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgenticNetworkParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgenticNetworkSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = AgenticNetworkIdEntry(document).nonEmpty

  override def spec: Spec = AgenticNetwork

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new AgenticNetworkReferenceHandler()
}
