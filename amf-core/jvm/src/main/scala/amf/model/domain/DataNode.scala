package amf.model.domain

import amf.core.model.domain
import amf.core.unsafe.PlatformSecrets

abstract class DataNode(private[amf] val dataNode: domain.DataNode) extends DomainElement {

  def name: String = dataNode.name

  def withName(name: String): this.type = {
    dataNode.withName(name)
    this
  }

  def canEqual(other: Any): Boolean
}

object DataNode extends PlatformSecrets {
  def apply(x: domain.DataNode): DataNode = platform.wrap(x)
}