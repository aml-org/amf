package amf.plugins.document.apicontract.model

import amf.core.client.scala.model.document.ExtensionLike
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.plugins.document.apicontract.metamodel.OverlayModel

class Overlay(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations) {
  override def meta = OverlayModel
}

object Overlay {
  def apply(): Overlay = apply(Annotations())

  def apply(annotations: Annotations): Overlay = new Overlay(Fields(), annotations)

}
