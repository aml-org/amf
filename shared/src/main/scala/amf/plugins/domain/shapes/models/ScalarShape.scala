package amf.plugins.domain.shapes.models

import amf.domain.Fields
import amf.framework.parser.Annotations
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel._
import org.yaml.model.YPart

/**
  * Scalar shape
  */
case class ScalarShape(fields: Fields, annotations: Annotations) extends Shape with CommonShapeFields {

  def dataType: String = fields(DataType)

  def withDataType(dataType: String): this.type = set(DataType, dataType)

  override def adopted(parent: String): this.type = withId(parent + "/scalar/" + name)

  override def linkCopy(): ScalarShape = ScalarShape().withId(id)

  override def meta = ScalarShapeModel
}

object ScalarShape {

  def apply(): ScalarShape = apply(Annotations())

  def apply(ast: YPart): ScalarShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ScalarShape = ScalarShape(Fields(), annotations)
}
