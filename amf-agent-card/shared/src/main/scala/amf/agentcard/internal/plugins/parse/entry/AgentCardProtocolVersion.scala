package amf.agentcard.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class AgentCardProtocolVersion(override val version: String) extends IdVersion(version)

object AgentCardProtocolVersion extends IdEntryVersion {

  override protected val idKey: String = "protocolVersion"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new AgentCardProtocolVersion(text))
  }
}
