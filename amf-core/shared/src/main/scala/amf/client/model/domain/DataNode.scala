package amf.client.model.domain

import amf.client.model.StrField
import amf.core.model.domain.{DataNode => InternalDataNode}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait DataNode extends DomainElement {

  override private[amf] val _internal: InternalDataNode

  def name: StrField = _internal.name

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
