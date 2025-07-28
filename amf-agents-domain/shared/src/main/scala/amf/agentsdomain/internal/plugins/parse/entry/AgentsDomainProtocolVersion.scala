package amf.agentsdomain.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class AgentsDomainProtocolVersion(override val version: String) extends IdVersion(version)

object AgentsDomainProtocolVersion extends IdEntryVersion {

  override protected val idKey: String = "apiVersion"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new AgentsDomainProtocolVersion(text))
  }
}
