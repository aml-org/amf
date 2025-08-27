package amf.shapes.internal.validation.jsonschema

import amf.core.client.scala.model.domain._
import amf.core.internal.annotations.LexicalInformation
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable

class LexicalProvider(element: DomainElement) {

  lazy private val map: mutable.Map[String, SourceLocation] = mutable.Map.empty

  def build(): this.type = {
    generatePathLexicalMap()
    this
  }

  private def generatePathLexicalMap(): Unit = element match {
    case node: DataNode => indexDataNode(node, "")
    case _              => // ignore
  }

  private def indexDataNode(node: DataNode, path: String): Unit = {
    map.put(path, node.annotations.sourceLocation)
    node match {
      case obj: ObjectNode    => indexObjectNode(obj, path)
      case array: ArrayNode   => indexArrayNode(array, path)
      case _                  => // Ignored. Scalars are already added in the first line of this method
    }
  }

  private def indexObjectNode(obj: ObjectNode, path: String): Unit = {
    obj.allPropertiesWithName().foreach { case (key, prop) =>
      indexDataNode(prop, computePath(path, key))
    }
  }

  private def indexArrayNode(array: ArrayNode, path: String): Unit = {
    array.members.zipWithIndex.foreach { case (member, position) =>
      indexDataNode(member, computePath(path, position.toString))
    }
  }

  private def computePath(base: String, part: String): String = base + "/" + part

  def findLocationInformation(path: String): Option[(LexicalInformation, String)] =
    findSourceLocation(path).map(s => (findLexical(s), s.sourceName))

  private def findSourceLocation(path: String): Option[SourceLocation] = map.get(path)

  private def findLexical(location: SourceLocation): LexicalInformation = LexicalInformation(location.range)
}

object LexicalProvider {
  def apply(element: DomainElement): LexicalProvider = new LexicalProvider(element).build()
}
