package amf.brokergroup.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion, IdVersion}

object BrokerGroupSchemaVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = BrokerGroupSchemaVersion
}
