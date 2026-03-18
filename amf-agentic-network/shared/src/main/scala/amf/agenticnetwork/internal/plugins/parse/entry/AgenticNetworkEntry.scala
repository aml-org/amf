package amf.agenticnetwork.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class AgenticNetworkEntry(override val version: String) extends IdVersion(version)

object AgenticNetworkEntry extends IdEntryVersion {

  override protected val idKey: String = "agentNetwork"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new AgenticNetworkEntry(text))
  }
}
