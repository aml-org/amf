package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.core.model.domain.{ArrayNode => InternalArrayNode}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.ArrayNode")
case class ArrayNode(override private[amf] val _internal: InternalArrayNode) extends DataNode {

  @JSExportTopLevel("model.domain.ArrayNode")
  def this() = this(InternalArrayNode())

  def members: ClientList[DataNode] = _internal.members.asClient

  def addMember(member: DataNode): this.type = {
    _internal.addMember(member._internal)
    this
  }
}
