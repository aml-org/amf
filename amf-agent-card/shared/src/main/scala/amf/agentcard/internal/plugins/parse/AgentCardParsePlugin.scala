package amf.agentcard.internal.plugins.parse

import amf.agentcard.internal.plugins.parse.entry.AgentCardProtocolVersionEntry
import amf.agentcard.internal.plugins.parse.schema.AgentCardSchemaLoader
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentCard, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentCardParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentCardSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = AgentCardProtocolVersionEntry(document).nonEmpty

  override def spec: Spec = AgentCard
}
