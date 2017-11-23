package amf.plugins.document.webapi.model

import amf.domain.Fields
import amf.framework.model.document.ExtensionLike
import amf.framework.parser.Annotations
import amf.plugins.document.webapi.metamodel.ExtensionModel

class Extension(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations) {
  override def meta = ExtensionModel
}

object Extension {
  def apply(): Extension = apply(Annotations())

  def apply(annotations: Annotations): Extension = new Extension(Fields(), annotations)

}