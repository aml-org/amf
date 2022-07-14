package amf.shapes.internal.spec.contexts

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.ParsedReference
import amf.core.internal.utils.UriUtils
import amf.shapes.internal.spec.common.parser.ShapeParserContext

object ReferenceFinder {

  def findJsonReferencedUnit(url: String, ctx: ShapeParserContext): Option[BaseUnit] = {
    val fileUrl     = getFileUrl(url, ctx.loc)
    val baseFileUrl = fileUrl.split("#").head
    ctx.refs
      .filter(r => r.unit.location().isDefined)
      .find(_.unit.location().get == baseFileUrl)
      .map(_.unit)
  }

  def findJsonReferencedUnit(fileUrl: String, references: Seq[ParsedReference]): Option[BaseUnit] = {
    val baseFileUrl = fileUrl.split("#").head
    references
      .filter(r => r.unit.location().isDefined)
      .find(_.unit.location().get == baseFileUrl)
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
