package amf.shapes.internal.plugins.parser.entry

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import org.yaml.model.YMap

trait IdEntry {

  protected def versionHandler: IdEntryVersion

  def apply(root: Root): Option[IdVersion] =
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(map) => versionHandler.parseIdVersion(map)
          case Left(_)    => None
        }
      case _ => None
    }
}
