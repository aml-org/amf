package amf.model

import scala.collection.JavaConverters._

abstract class DataNode(private[amf] val dataNode: amf.domain.extensions.DataNode) extends DomainElement {

  def name: String = dataNode.name

  def withName(name: String): this.type = {
    dataNode.withName(name)
    this
  }

  def canEqual(other: Any): Boolean
}

object DataNode {
  def apply(x: amf.domain.extensions.DataNode): DataNode = x match {
    case o: amf.domain.extensions.ObjectNode => ObjectNode(o)
    case s: amf.domain.extensions.ScalarNode => ScalarNode(s)
    case a: amf.domain.extensions.ArrayNode  => ArrayNode(a)
    case _                                   => throw new Exception(s"Unknown data node type $x")
  }
}

case class ObjectNode(private[amf] val objectNode: amf.domain.extensions.ObjectNode) extends DataNode(objectNode) {

  def properties: java.util.Map[String, java.util.List[DataNode]] =
    objectNode.properties
      .map({ (p) =>
        val key     = p._1
        val objects = p._2.map(DataNode(_)).asJava
        key -> objects
      })
      .asJava

  def addProperty(property: String, objectValue: DataNode): this.type = {
    objectNode.addProperty(property, objectValue.dataNode)
    this
  }

  override private[amf] def element = objectNode

  def this() = this(amf.domain.extensions.ObjectNode())

}

case class ScalarNode(private[amf] val scalarNode: amf.domain.extensions.ScalarNode) extends DataNode(scalarNode) {

  val value: String = scalarNode.value
  val dataType: String = scalarNode.dataType match {
    case Some(t) => t
    case None    => null
  }

  override private[amf] def element = scalarNode

  def this() = this(amf.domain.extensions.ScalarNode())

}

object ScalarNode {
  def build(value: String, dataType: String) = ScalarNode(amf.domain.extensions.ScalarNode(value, Option(dataType)))
}

case class ArrayNode(private[amf] val arrayNode: amf.domain.extensions.ArrayNode) extends DataNode(arrayNode) {

  def members: java.util.List[DataNode] = arrayNode.members.map(DataNode(_)).asJava

  def addMember(member: DataNode): this.type = {
    arrayNode.addMember(member.dataNode)
    this
  }

  override private[amf] def element = arrayNode

  def this() = this(amf.domain.extensions.ArrayNode())

}
