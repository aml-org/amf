package amf.agentgraph.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}


object AgentGraphIdEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentGraphEntry
}
