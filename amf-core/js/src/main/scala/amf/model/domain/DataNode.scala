package amf.model.domain

import amf.core.model.domain
import amf.core.unsafe.PlatformSecrets

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.domain.DataNode")
@JSExportAll
class DataNode(private[amf] val dataNode: domain.DataNode) extends DomainElement {

  def name: String = dataNode.name

  def withName(name: String): this.type = {
    dataNode.withName(name)
    this
  }

  def canEqual(other: Any): Boolean = throw new Exception("DataNode is abstract")

  override private[amf] def element: domain.DataNode = throw new Exception("DataNode is abstract")
}

object DataNode extends PlatformSecrets {
  // This is dynamic, cannot be done through the registry
  def apply(x: domain.DataNode): DataNode = x match {
    case o: domain.ObjectNode => ObjectNode(o)
    case s: domain.ScalarNode => ScalarNode(s)
    case a: domain.ArrayNode  => ArrayNode(a)
  }
}

@JSExportAll
case class ObjectNode(private[amf] val objectNode: domain.ObjectNode) extends DataNode(objectNode) {

  def properties: js.Dictionary[DataNode] =
    objectNode.properties
      .map({ (p) =>
        val key = p._1
        val obj = DataNode(p._2)
        key -> obj
      })
      .toJSDictionary

  def addProperty(property: String, objectValue: DataNode): this.type = {
    objectNode.addProperty(property, objectValue.dataNode)
    this
  }

  override private[amf] def element: domain.ObjectNode = objectNode

  def this() = this(domain.ObjectNode())

}

@JSExportAll
case class ScalarNode(private[amf] val scalarNode: domain.ScalarNode) extends DataNode(scalarNode) {

  val value: String    = scalarNode.value
  val dataType: String = scalarNode.dataType.orNull

  override private[amf] def element: domain.ScalarNode = scalarNode

  def this() = this(domain.ScalarNode())

}

@JSExportAll
object ScalarNode {
  def build(value: String, dataType: String) = ScalarNode(domain.ScalarNode(value, Option(dataType)))
}

@JSExportAll
case class ArrayNode(private[amf] val arrayNode: domain.ArrayNode) extends DataNode(arrayNode) {

  def members: js.Iterable[DataNode] = arrayNode.members.map(DataNode(_)).toJSArray

  def addMember(member: DataNode): this.type = {
    arrayNode.addMember(member.dataNode)
    this
  }

  override private[amf] def element: domain.ArrayNode = arrayNode

  def this() = this(domain.ArrayNode())

}