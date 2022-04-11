package amf.shapes.client.scala.model.domain.operations

import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeResponseModel
import amf.shapes.internal.domain.metamodel.operations.ShapeResponseModel.Payload
import org.yaml.model.YPart

case class ShapeResponse(override val fields: Fields, override val annotations: Annotations) extends AbstractResponse {
  override type PayloadType = ShapePayload

  override def payload: PayloadType                         = fields.field(Payload)
  override def withPayload(payload: PayloadType): this.type = set(Payload, payload)

  override def meta: ShapeResponseModel.type = ShapeResponseModel
}

object ShapeResponse {
  def apply(): ShapeResponse = apply(Annotations())

  def apply(ast: YPart): ShapeResponse = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapeResponse = new ShapeResponse(Fields(), annotations)
}
