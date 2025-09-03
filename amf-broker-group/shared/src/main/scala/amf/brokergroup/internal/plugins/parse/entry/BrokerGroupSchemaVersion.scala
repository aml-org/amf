package amf.brokergroup.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

class BrokerGroupSchemaVersion(override val version: String) extends IdVersion(version)

object BrokerGroupSchemaVersion extends IdEntryVersion {

  override protected val idKey: String = "ansVersion"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    // No fixed versions at the moment, so any text could be a version
    Some(new BrokerGroupSchemaVersion(text))
  }
}
