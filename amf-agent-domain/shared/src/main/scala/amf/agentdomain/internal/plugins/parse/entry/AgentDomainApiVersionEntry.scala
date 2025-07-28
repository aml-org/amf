package amf.agentdomain.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}

object AgentDomainApiVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentDomainProtocolVersion
}
