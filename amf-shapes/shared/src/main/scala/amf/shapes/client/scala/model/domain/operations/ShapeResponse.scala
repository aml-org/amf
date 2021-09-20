package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeResponseModel
import org.yaml.model.YMapEntry

/**
  * Response internal model.
  */
case class ShapeResponse(override val fields: Fields, override val annotations: Annotations)
  extends NamedDomainElement {

  def payload: ShapePayload                          = fields.field(ShapeResponseModel.Payload)
  def withPayload(payload: ShapePayload): this.type  = set(ShapeResponseModel.Payload, payload)

  override def meta: ShapeResponseModel.type = ShapeResponseModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/response"

  override def nameField: Field = ShapeResponseModel.Name
}

object ShapeResponse {
  def apply(): ShapeResponse = apply(Annotations())

  def apply(entry: YMapEntry): ShapeResponse = apply(Annotations(entry))

  def apply(annotations: Annotations): ShapeResponse = new ShapeResponse(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): ShapeResponse = new ShapeResponse(fields, annotations)
}
