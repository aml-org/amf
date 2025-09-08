package amf.brokergroup.internal.plugins.parse

import amf.brokergroup.internal.plugins.parse.entry.BrokerGroupSchemaVersionEntry
import amf.brokergroup.internal.plugins.parse.schema.BrokerGroupSchemaLoader
import amf.core.internal.parser.Root
import amf.core.internal.remote.{BrokerGroup, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object BrokerGroupParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = BrokerGroupSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = BrokerGroupSchemaVersionEntry(document).nonEmpty

  override def spec: Spec = BrokerGroup
}
