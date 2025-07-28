package amf.agentdomain.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}

object AgentDomainSchemaVersionEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = AgentDomainSchemaVersion
}
