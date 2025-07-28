package amf.agentdomain.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class AgentDomainSchemaVersion(override val version: String) extends IdVersion(version)

object AgentDomainSchemaVersion extends IdEntryVersion {

  override protected val idKey: String = "schemaVersion"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new AgentDomainSchemaVersion(text))
  }
}
