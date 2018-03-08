package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.plugins.domain.shapes.models.{NilShape => InternalNilShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class NilShape(override private[amf] val _internal: InternalNilShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.NilShape")
  def this() = this(InternalNilShape())

  override def linkTarget: Option[DomainElement] = _internal.linkTarget.map({ case l: InternalNilShape => l }).asClient

  override def linkCopy(): NilShape = _internal.linkCopy()
}
