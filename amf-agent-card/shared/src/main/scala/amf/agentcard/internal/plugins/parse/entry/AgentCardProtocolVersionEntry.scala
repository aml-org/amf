package amf.agentcard.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}


object AgentCardProtocolVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentCardProtocolVersion
}
