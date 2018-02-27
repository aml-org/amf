package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

class DomainExtension(val fields: Fields, val annotations: Annotations)
    extends BaseDomainExtension
    with DomainElement {
  override def meta: DomainExtensionModel = DomainExtensionModel
}

object DomainExtension {
  def apply(): DomainExtension = apply(Annotations())

  def apply(ast: YPart): DomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): DomainExtension = new DomainExtension(Fields(), annotations)
}
