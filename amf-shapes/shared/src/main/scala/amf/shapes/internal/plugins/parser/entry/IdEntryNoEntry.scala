package amf.shapes.internal.plugins.parser.entry

import amf.core.internal.parser.Root

// This is used if there is no version entry
class IdEntryNoEntry extends IdEntry {

  // This is protected in case you want to override the default one
  protected def defaultVersion: IdVersion = DefaultIdVersion

  override def apply(root: Root): Option[IdVersion] = Some(defaultVersion)

  override protected def versionHandler: IdEntryVersion = IdEntryVersionNoEntry
}

object DefaultIdVersion extends IdVersion("1.0.0")

object IdEntryVersionNoEntry extends IdEntryVersion {
  override protected val idKey: String                                           = ""
  override protected def getIdVersionFromString(text: String): Option[IdVersion] = None
}
