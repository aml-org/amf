package amf.plugins.domain.shapes.models

import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel._
import org.yaml.model.YPart

/**
  * Scalar shape
  */
case class ScalarShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with CommonShapeFields {

  def dataType: StrField = fields.field(DataType)

  def withDataType(dataType: String): this.type = set(DataType, dataType)

  override def linkCopy(): ScalarShape = ScalarShape().withId(id)

  override def meta: Obj = ScalarShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/scalar/" + name.value()
}

object ScalarShape {
  def apply(): ScalarShape = apply(Annotations())

  def apply(ast: YPart): ScalarShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ScalarShape = ScalarShape(Fields(), annotations)
}
