package amf.xml.internal.plugins.syntax

import amf.xml.internal.spec.context.XMLDocContext

import scala.xml.Elem

trait XMLDocumentParserHelper {

  def instanceClass(elem: Elem, ctx: XMLDocContext): String = {
    val label = elem.label
    val parts = label.split(Array(' ', '-', '_'))
    val suffix = parts.map(_.capitalize).mkString
    ctx.resolveNamespace(elem.prefix) + "#" + suffix
  }

  def propertyIri(key: String, elem: Elem, ctx: XMLDocContext): String = {
    if (key.contains(':')) {
      val parts = key.split(":")
      val pref = parts(0)
      val label = parts(1)
      ctx.resolveNamespace(pref) + "#" +  label
    } else {
      elem.namespace + "#" + key
    }
  }
}
