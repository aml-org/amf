package amf.model.domain

import amf.core.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.domain.RecursiveShape")
@JSExportAll
class RecursiveShape(private[amf] override val shape: domain.RecursiveShape) extends Shape(shape) {

  def fixpoint: String = shape.fixpoint
  
  def withFixPoint(shapeId: String) = shape.withFixPoint(shapeId)

  override private[amf] def element = shape

  override def linkTarget: Option[DomainElement with Linkable] = shape.linkTarget.map(t => platform.wrap[DomainElement with Linkable](t))

  override def linkCopy(): DomainElement with Linkable = new RecursiveShape(domain.RecursiveShape())
}
