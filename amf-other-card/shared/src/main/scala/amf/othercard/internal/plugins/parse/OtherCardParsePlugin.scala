package amf.othercard.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{OtherCard, Spec}
import amf.othercard.internal.plugins.parse.entry.OtherCardIdEntry
import amf.othercard.internal.plugins.parse.schema.OtherCardSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object OtherCardParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = OtherCardSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = OtherCardIdEntry(document).nonEmpty

  override def spec: Spec = OtherCard
}
