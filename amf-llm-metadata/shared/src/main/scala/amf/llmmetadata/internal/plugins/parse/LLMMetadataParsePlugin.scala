package amf.llmmetadata.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{LLMMetadata, Mimes, Spec}
import amf.llmmetadata.internal.plugins.parse.entry.LLMMetadataIdEntry
import amf.llmmetadata.internal.plugins.parse.schema.LLMMetadataSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object LLMMetadataParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = LLMMetadataSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean =
    if (document.mediatype == Mimes.`application/ld+json`) true else LLMMetadataIdEntry(document).nonEmpty

  override def spec: Spec = LLMMetadata
}
