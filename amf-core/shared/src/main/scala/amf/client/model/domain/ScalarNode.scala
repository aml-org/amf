package amf.client.model.domain

import amf.core.model.domain.{ScalarNode => InternalScalarNode}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.ScalarNode")
case class ScalarNode(override private[amf] val _internal: InternalScalarNode) extends DataNode {

  @JSExportTopLevel("model.domain.ScalarNode")
  def this() = this(InternalScalarNode())

  @JSExportTopLevel("model.domain.ScalarNode")
  def this(value: String, dataType: String) = this(InternalScalarNode(value, Option(dataType)))

  def value: String    = _internal.value
  def dataType: String = _internal.dataType.orNull

  override def toString = s"${name.value()}:$dataType=$value"
}

@JSExportAll
object ScalarNode {
  def build(value: String, dataType: String) = new ScalarNode(value, dataType)
}
