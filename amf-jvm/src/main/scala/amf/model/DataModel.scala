package amf.model

import amf.core.model.domain
import amf.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}

import scala.collection.JavaConverters._

abstract class DataNode(private[amf] val dataNode: domain.DataNode) extends DomainElement {

  def name: String = dataNode.name

  def withName(name: String): this.type = {
    dataNode.withName(name)
    this
  }

  def canEqual(other: Any): Boolean
}

object DataNode {
  def apply(x: domain.DataNode): domain.DataNode = x match {
    case o: domain.ObjectNode => ObjectNode(o)
    case s: domain.ScalarNode => domain.ScalarNode(s)
    case a: domain.ArrayNode  => ArrayNode(a)
    case _                                   => throw new Exception(s"Unknown data node type $x")
  }
}

case class ObjectNode(private[amf] val objectNode: domain.ObjectNode) extends domain.DataNode(objectNode) {

  def properties: java.util.Map[String, domain.DataNode] =
    objectNode.properties
      .map({ (p) =>
        val key = p._1
        val obj = domain.DataNode(p._2)
        key -> obj
      })
      .asJava

  def addProperty(property: String, objectValue: domain.DataNode): this.type = {
    objectNode.addProperty(property, objectValue.dataNode)
    this
  }

  override private[amf] def element = objectNode

  def this() = this(domain.ObjectNode())

}

case class ScalarNode(private[amf] val scalarNode: domain.ScalarNode) extends domain.DataNode(scalarNode) {

  val value: String    = scalarNode.value
  val dataType: String = scalarNode.dataType.orNull

  override private[amf] def element = scalarNode

  def this() = this(domain.ScalarNode())

}

object ScalarNode {
  def build(value: String, dataType: String) = ScalarNode(domain.ScalarNode(value, Option(dataType)))
}

case class ArrayNode(private[amf] val arrayNode: domain.ArrayNode) extends domain.DataNode(arrayNode) {

  def members: java.util.List[domain.DataNode] = arrayNode.members.map(DataNode(_)).asJava

  def addMember(member: domain.DataNode): this.type = {
    arrayNode.addMember(member.dataNode)
    this
  }

  override private[amf] def element = arrayNode

  def this() = this(domain.ArrayNode())

}
