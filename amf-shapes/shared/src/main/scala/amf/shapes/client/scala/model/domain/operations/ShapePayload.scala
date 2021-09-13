package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.{NamedDomainElement, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.operations.ShapePayloadModel
import org.yaml.model.YPart

/**
  * Payload internal model.
  */
case class ShapePayload(fields: Fields, annotations: Annotations)
  extends NamedDomainElement {

  def schema: Shape             = fields.field(ShapePayloadModel.Schema)

  def withSchema(schema: Shape): this.type              = set(ShapePayloadModel.Schema, schema)

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(ShapePayloadModel.Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(ShapePayloadModel.Schema, scalar)
    scalar
  }

  def withArraySchema(name: String): ArrayShape = {
    val array = ArrayShape().withName(name)
    set(ShapePayloadModel.Schema, array)
    array
  }


  override def meta: ShapePayloadModel.type = ShapePayloadModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    "/" + name.value().urlComponentEncoded

  override def nameField: Field = ShapePayloadModel.Name
}

object ShapePayload {
  def apply(): ShapePayload = apply(Annotations())

  def apply(ast: YPart): ShapePayload = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapePayload = new ShapePayload(Fields(), annotations)
}

