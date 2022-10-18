package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{Linkable, NamedDomainElement, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.operations.AbstractPayloadModel
import amf.shapes.internal.domain.metamodel.operations.AbstractPayloadModel._

/** Payload internal model.
  */
abstract class AbstractPayload(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable {

  def schema: Shape       = fields.field(Schema)
  def mediaType: StrField = fields.field(MediaType)

  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)
  def withSchema(schema: Shape): this.type        = set(Schema, schema)

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(Schema, scalar)
    scalar
  }

  def withArraySchema(name: String): ArrayShape = {
    val array = ArrayShape().withName(name)
    set(Schema, array)
    array
  }

  override def meta: AbstractPayloadModel = AbstractPayloadModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + name.value().urlComponentEncoded

  override def nameField: Field = Name

}
