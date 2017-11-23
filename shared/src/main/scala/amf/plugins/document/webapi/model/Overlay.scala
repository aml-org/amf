package amf.plugins.document.webapi.model

import amf.domain.Fields
import amf.framework.model.document.ExtensionLike
import amf.framework.parser.Annotations
import amf.plugins.document.webapi.metamodel.OverlayModel

class Overlay(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations) {
  override def meta = OverlayModel
}

object Overlay {
  def apply(): Overlay = apply(Annotations())

  def apply(annotations: Annotations): Overlay = new Overlay(Fields(), annotations)

}