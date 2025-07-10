package amf.agentfabric.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}

object AgentFabricApiVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentFabricProtocolVersion
}
