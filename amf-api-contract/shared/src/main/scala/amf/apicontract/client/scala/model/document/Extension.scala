package amf.apicontract.client.scala.model.document

import amf.apicontract.internal.metamodel.document.ExtensionModel
import amf.core.client.scala.model.document.ExtensionLike
import amf.core.internal.parser.domain.{Annotations, Fields}

class Extension(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations) {
  override def meta = ExtensionModel
}

object Extension {
  def apply(): Extension = apply(Annotations())

  def apply(annotations: Annotations): Extension = new Extension(Fields(), annotations)

}
