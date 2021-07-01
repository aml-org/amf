package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.ArrayShapeModel.Items
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, MatrixShapeModel}
import org.yaml.model.YPart

case class MatrixShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Shape = fields.field(Items)

  def withItems(items: Shape): this.type = set(Items, items)

  def toArrayShape = {
    val array = ArrayShape(fields, annotations)
    Option(id) match {
      case Some(effectiveId: String) =>
        val res = array.withId(effectiveId)
        res
      case None =>
        array
    }
  }

  def toMatrixShape: MatrixShape = this

  override def linkCopy() = MatrixShape().withId(id)

  override val meta: AnyShapeModel = MatrixShapeModel

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = MatrixShape.apply
}

object MatrixShape {

  def apply(): MatrixShape = apply(Annotations())

  def apply(ast: YPart): MatrixShape = apply(Annotations(ast))

  def apply(annotations: Annotations): MatrixShape = MatrixShape(Fields(), annotations)

}
