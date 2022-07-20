package amf.shapes.internal.spec.contexts

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.ParsedReference
import amf.core.internal.utils.UriUtils
import amf.shapes.internal.spec.common.parser.ShapeParserContext

object ReferenceFinder {

  def findJsonReferencedUnit(fileUrl: String, rawRef: String, references: Seq[ParsedReference]): Option[BaseUnit] = {
    val baseFileUrl = fileUrl.split("#").head
    val baseRawRef  = rawRef.split("#").head
    references
      .filter(r => r.unit.location().isDefined)
      .find(ref => ref.unit.location().get == baseFileUrl)
      .orElse(references.find(ref => ref.origin.url == baseRawRef))
      .map(_.unit)
  }

  def getFileUrl(ref: String, base: String): String = UriUtils.resolveRelativeTo(base, ref)

  def getJsonReferenceFragment(fileUrl: String): Option[String] = {
    fileUrl.split("#") match {
      case s: Array[String] if s.size > 1 => Some(s.last)
      case _                              => None
    }
  }
}
