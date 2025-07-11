package amf.agentfabric.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentFabric, Mimes, Spec}
import amf.agentfabric.internal.plugins.parse.entry.AgentFabricApiVersionEntry
import amf.agentfabric.internal.plugins.parse.schema.AgentFabricSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentFabricParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentFabricSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean =
    if (document.mediatype == Mimes.`application/ld+json`) true else AgentFabricApiVersionEntry(document).nonEmpty

  override def spec: Spec = AgentFabric
}
