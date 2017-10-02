package amf.shape

import amf.domain.{Annotations, Fields}
import amf.metadata.shape.ScalarShapeModel._
import org.yaml.model.YPart

/**
  * Scalar shape
  */
case class ScalarShape(fields: Fields, annotations: Annotations) extends Shape with CommonOASFields {

  def dataType: String         = fields(DataType)

  def withDataType(dataType: String): this.type    = set(DataType, dataType)

  override def adopted(parent: String): this.type = withId(parent + "/scalar/" + name)
}

object ScalarShape {

  def apply(): ScalarShape = apply(Annotations())

  def apply(ast: YPart): ScalarShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ScalarShape = ScalarShape(Fields(), annotations)
}
