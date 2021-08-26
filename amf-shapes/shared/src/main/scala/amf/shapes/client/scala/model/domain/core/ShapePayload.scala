package amf.shapes.client.scala.model.domain.core

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel.Name
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.core.{ShapeParameterModel, ShapePayloadModel}
import amf.shapes.internal.domain.metamodel.core.ShapePayloadModel.{MediaType, Schema}
import org.yaml.model.YPart

case class ShapePayload(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def mediaType: StrField = fields.field(MediaType)
  def schema: Shape       = fields.field(Schema)

  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)
  def withSchema(schema: Shape): this.type        = set(Schema, schema)

  def setSchema(shape: Shape): Shape = {
    set(ShapeParameterModel.Schema, shape)
    shape
  }

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

  override def linkCopy(): ShapePayload = ShapePayload().withId(id)

  override def meta: ShapePayloadModel.type = ShapePayloadModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    "/" + mediaType
      .option()
      .getOrElse(name.option().getOrElse("default"))
      .urlComponentEncoded // todo: / char of media type should be encoded?
  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ShapePayload.apply
  override def nameField: Field                                                                 = Name
}

object ShapePayload {
  def apply(): ShapePayload = apply(Annotations())

  def apply(ast: YPart): ShapePayload = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapePayload = new ShapePayload(Fields(), annotations)
}
