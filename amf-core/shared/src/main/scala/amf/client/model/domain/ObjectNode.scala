package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.core.model.domain.{ObjectNode => InternalObjectNode}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.ObjectNode")
case class ObjectNode(override private[amf] val _internal: InternalObjectNode) extends DataNode {

  @JSExportTopLevel("model.domain.ObjectNode")
  def this() = this(InternalObjectNode())

  def properties: ClientMap[DataNode] = _internal.properties.asClient

  def addProperty(property: String, node: DataNode): this.type = {
    _internal.addProperty(property, node._internal)
    this
  }
}
