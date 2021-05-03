package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.plugins.document.webapi.parser.spec.declaration.common.YMapEntryLike

import java.net.URI
import scala.collection.mutable

case class AstIndex(private val map: mutable.Map[String, YMapEntryLike], resolvers: Seq[ReferenceResolver]) {

  def getNode(reference: String): Option[YMapEntryLike] = {
    val toLookUp = clean(reference)
    map.get(toLookUp).orElse {
      callResolvers(toLookUp)
    }
  }

  private def callResolvers(reference: String): Option[YMapEntryLike] =
    resolvers.iterator.map(_.resolve(reference, map.toMap)).collectFirst {
      case Some(entry) =>
        map.put(reference, entry)
        entry
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

  private[jsonschema] def valueInMap(k: String): Option[YMapEntryLike] = map.get(k)
}
