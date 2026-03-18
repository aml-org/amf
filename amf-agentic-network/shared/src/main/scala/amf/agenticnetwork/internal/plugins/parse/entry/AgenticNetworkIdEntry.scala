package amf.agenticnetwork.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}


object AgenticNetworkIdEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgenticNetworkEntry
}
