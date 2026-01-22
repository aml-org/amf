package amf.agentgraph.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class AgentGraphEntry(override val version: String) extends IdVersion(version)

object AgentGraphEntry extends IdEntryVersion {

  override protected val idKey: String = "agent-graph"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new AgentGraphEntry(text))
  }
}
