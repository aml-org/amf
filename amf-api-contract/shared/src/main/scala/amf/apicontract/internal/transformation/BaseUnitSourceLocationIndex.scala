package amf.apicontract.internal.transformation

import amf.apicontract.internal.transformation.BaseUnitSourceLocationIndex.resolveUri
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.annotations.SourceLocation

import scala.collection.mutable.ArrayBuffer

/** Indexes Base Units by their SourceLocation
  */
sealed case class BaseUnitSourceLocationIndex(index: Map[String, BaseUnit]) {
  def get(sourceLocation: String): Option[BaseUnit] = index.get(resolveUri(sourceLocation))
}

object BaseUnitSourceLocationIndex {
  def build(root: BaseUnit): BaseUnitSourceLocationIndex = {
    val index = flattenReferencesTree(root)
      .flatMap(bu =>
        bu.annotations
          .find(classOf[SourceLocation])
          .map(annotation => resolveUri(annotation.location) -> bu)
      )
      .toMap
    BaseUnitSourceLocationIndex(index)
  }

  private def flattenReferencesTree(root: BaseUnit): Seq[BaseUnit] =
    root +: (root.references ++ root.references.flatMap(_.references))

  private def resolveUri(uri: String): String = {
    val protocolRegex = "\\w+://".r
    val protocol      = protocolRegex.findPrefixOf(uri).getOrElse("")
    val path          = uri.stripPrefix(protocol)
    val segments      = path.split("/")

    val resolvedSegments = ArrayBuffer.empty[String]

    segments.foreach {
      case "." => // do nothing
      case ".." =>
        val lastIndex = resolvedSegments.length - 1
        resolvedSegments.remove(lastIndex)
      case segment => resolvedSegments.append(segment)
    }

    val r = s"$protocol${resolvedSegments.mkString("/")}"
    r
  }

}
