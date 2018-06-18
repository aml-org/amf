package amf.client.model.domain

import amf.plugins.domain.shapes.models.{ArrayShape => InternalArrayShape, MatrixShape => InternalMatrixShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class MatrixShape(override private[amf] val _internal: InternalArrayShape) extends ArrayShape(_internal) {

  @JSExportTopLevel("model.domain.MatrixShape")
  def this() = this(InternalArrayShape())

  override def withItems(items: Shape): this.type = {
    items match {
      case array: ArrayShape => super.withItems(items)
      case _                 => throw new Exception("Matrix shapes can only accept arrays as items")
    }
  }
}

/*object MatrixShape {
  def apply(matrix: InternalMatrixShape): MatrixShape = { new MatrixShape(matrix.toArrayShape) }
}*/
