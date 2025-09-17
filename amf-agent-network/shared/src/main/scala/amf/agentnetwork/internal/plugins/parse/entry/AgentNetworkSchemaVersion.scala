package amf.agentnetwork.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class AgentNetworkSchemaVersion(override val version: String) extends IdVersion(version)

object AgentNetworkSchemaVersion extends IdEntryVersion {

  override protected val idKey: String = "schemaVersion"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new AgentNetworkSchemaVersion(text))
  }
}
