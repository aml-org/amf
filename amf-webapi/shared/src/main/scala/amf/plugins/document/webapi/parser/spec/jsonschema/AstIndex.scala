package amf.plugins.document.webapi.parser.spec.jsonschema

import java.net.URI

import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike

import scala.collection.mutable

case class AstIndex(private val map: mutable.Map[String, YMapEntryLike]) {

  def getNode(reference: String): Option[YMapEntryLike] = {
    val toLookUp = clean(reference)
    map.get(toLookUp)
  }

  private def clean(reference: String): String = {
    if (reference.startsWith("#/")) reference.drop(1)
    else if (reference.equals("#")) "/"
    else if (reference.endsWith("#")) reference.dropRight(1)
    else if (!isUriAbsolute(reference) && !reference.startsWith("/")) s"/$reference"
    else reference
  }

  private def isUriAbsolute(reference: String) = {
    try {
      new URI(reference).isAbsolute
    } catch {
      case _: Throwable => false
    }
  }
}