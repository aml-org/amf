package amf.agentsdomain.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentsDomain, Mimes, Spec}
import amf.agentsdomain.internal.plugins.parse.entry.AgentsDomainApiVersionEntry
import amf.agentsdomain.internal.plugins.parse.schema.AgentsDomainSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentsDomainParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentsDomainSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean =
    if (document.mediatype == Mimes.`application/ld+json`) true else AgentsDomainApiVersionEntry(document).nonEmpty

  override def spec: Spec = AgentsDomain
}
