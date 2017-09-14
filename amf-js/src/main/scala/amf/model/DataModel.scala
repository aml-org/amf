package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class DataNode (private[amf] val dataNode: amf.domain.extensions.DataNode) extends DomainElement {

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
    case _ => throw new Exception(s"Unknown data node type $x")
  }
}

@JSExportAll
case class ObjectNode(private[amf] val objectNode: amf.domain.extensions.ObjectNode) extends DataNode(objectNode) {

  def properties: js.Dictionary[js.Iterable[DataNode]] = objectNode.properties.map({ (p) =>
    val key = p._1
    val objects = p._2.map(DataNode(_)).toJSIterable
    key -> objects
  }).toJSDictionary

  def addProperty(property: String, objectValue: DataNode): this.type = {
    objectNode.addProperty(property, objectValue.dataNode)
    this
  }

  override private[amf] def element = objectNode

  def this() = this(amf.domain.extensions.ObjectNode())

  override def equals(other: Any): Boolean = other match {
    case that: ObjectNode =>
      (that canEqual this) &&
        objectNode == that.objectNode
    case _ => false
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[ObjectNode]
}

@JSExportAll
case class ScalarNode(private[amf] val scalarNode: amf.domain.extensions.ScalarNode) extends DataNode(scalarNode) {

  val value: String = scalarNode.value
  val dataType: String = scalarNode.dataType match {
    case Some(t) => t
    case None    => null
  }

  override private[amf] def element = scalarNode

  def this() = this(amf.domain.extensions.ScalarNode())

  override def equals(other: Any): Boolean = other match {
    case that: ScalarNode =>
      (that canEqual this) &&
        scalarNode == that.scalarNode
    case _ => false
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[ScalarNode]
}

@JSExportAll
object ScalarNode {
  def build(value: String, dataType: String) = ScalarNode(amf.domain.extensions.ScalarNode(value, Option(dataType)))
}

@JSExportAll
case class ArrayNode(private[amf] val arrayNode: amf.domain.extensions.ArrayNode) extends DataNode(arrayNode) {

  def members: js.Iterable[DataNode] = arrayNode.members.map(DataNode(_)).toJSArray

  def addMember(member: DataNode): this.type = {
    arrayNode.addMember(member.dataNode)
    this
  }

  override private[amf] def element = arrayNode

  def this() = this(amf.domain.extensions.ArrayNode())

  override def equals(other: Any): Boolean = other match {
    case that: ArrayNode =>
      (that canEqual this) &&
        arrayNode == that.arrayNode
    case _ => false
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[ArrayNode]
}