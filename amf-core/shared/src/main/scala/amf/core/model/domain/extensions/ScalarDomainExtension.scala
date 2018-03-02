package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.ScalarDomainExtensionModel
import amf.core.metamodel.domain.extensions.ScalarDomainExtensionModel.Element
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class ScalarDomainExtension(fields: Fields, annotations: Annotations) extends BaseDomainExtension {

  def element: String = fields(Element)

  def withElement(element: String): this.type = set(Element, name)

  override def meta = ScalarDomainExtensionModel
}

object ScalarDomainExtension {
  def apply(): BaseDomainExtension = apply(Annotations())

  def apply(ast: YPart): BaseDomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): BaseDomainExtension = ScalarDomainExtension(Fields(), annotations)
}
