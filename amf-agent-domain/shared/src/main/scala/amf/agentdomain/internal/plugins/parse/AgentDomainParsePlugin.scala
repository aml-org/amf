package amf.agentdomain.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentDomain, Mimes, Spec}
import amf.agentdomain.internal.plugins.parse.entry.AgentDomainApiVersionEntry
import amf.agentdomain.internal.plugins.parse.schema.AgentDomainSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentDomainParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentDomainSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean =
    if (document.mediatype == Mimes.`application/ld+json`) true else AgentDomainApiVersionEntry(document).nonEmpty

  override def spec: Spec = AgentDomain
}
