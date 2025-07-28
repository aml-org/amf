package amf.agentsdomain.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}

object AgentsDomainApiVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentsDomainProtocolVersion
}
