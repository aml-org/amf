package amf.apicontract.client.scala.model.document

import amf.apicontract.internal.metamodel.document.OverlayModel
import amf.core.client.scala.model.document.ExtensionLike
import amf.core.internal.parser.domain.{Annotations, Fields}

class Overlay(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations) {
  override def meta = OverlayModel
}

object Overlay {
  def apply(): Overlay = apply(Annotations())

  def apply(annotations: Annotations): Overlay = new Overlay(Fields(), annotations)

}
