package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class DomainExtension(fields: Fields, annotations: Annotations) extends BaseDomainExtension {
  override def meta: DomainExtensionModel = DomainExtensionModel
}

object DomainExtension {
  def apply(): BaseDomainExtension = apply(Annotations())

  def apply(ast: YPart): BaseDomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): BaseDomainExtension = new DomainExtension(Fields(), annotations)
}
