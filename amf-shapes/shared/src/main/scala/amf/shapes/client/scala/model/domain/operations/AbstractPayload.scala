package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractPayloadModel
import org.yaml.model.YPart

/**
  * Payload internal model.
  */
abstract class AbstractPayload(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable {

  def schema: Shape = fields.field(AbstractPayloadModel.Schema)

  def withSchema(schema: Shape): this.type = set(AbstractPayloadModel.Schema, schema)

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(AbstractPayloadModel.Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(AbstractPayloadModel.Schema, scalar)
    scalar
  }

  def withArraySchema(name: String): ArrayShape = {
    val array = ArrayShape().withName(name)
    set(AbstractPayloadModel.Schema, array)
    array
  }

  override def meta: DomainElementModel = AbstractPayloadModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    "/" + name.value().urlComponentEncoded

  override def nameField: Field = AbstractPayloadModel.Name

}
