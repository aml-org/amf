package amf.agentnetwork.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion, IdVersion}

object AgentNetworkSchemaVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentNetworkSchemaVersion
}
