package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class NilShape(private[amf] val nil: models.NilShape) extends AnyShape(nil) {

  @JSExportTopLevel("model.domain.NilShape")
  def this() = this(models.NilShape())

  override private[amf] def element = nil

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.NilShape => NilShape(l) })

  override def linkCopy(): DomainElement with Linkable = NilShape(element.linkCopy())
}
