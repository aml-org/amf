package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.StrField
import amf.core.model.domain.{RecursiveShape => InternalRecursiveShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.RecursiveShape")
case class RecursiveShape(private[amf] override val _internal: InternalRecursiveShape) extends Shape {

  def fixpoint: StrField = _internal.fixpoint

  def withFixPoint(shapeId: String): this.type = {
    _internal.withFixPoint(shapeId)
    this
  }

  override def linkCopy(): Linkable = throw new Exception("Recursive shape cannot be linked")
}
