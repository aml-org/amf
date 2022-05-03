package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapePayloadModel
import org.yaml.model.YPart

case class ShapePayload(override val fields: Fields, override val annotations: Annotations)
    extends AbstractPayload(fields, annotations) {
  override def linkCopy(): ShapePayload = ShapePayload().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ShapePayload.apply

  override def meta: ShapePayloadModel.type = ShapePayloadModel
}

object ShapePayload {
  def apply(): ShapePayload = apply(Annotations())

  def apply(ast: YPart): ShapePayload = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapePayload = new ShapePayload(Fields(), annotations)
}
