package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.DomainScalarExtensionModel
import amf.core.metamodel.domain.extensions.DomainScalarExtensionModel.Element
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class DomainScalarExtension(fs: Fields, as: Annotations) extends DomainExtension(fs, as) with DomainElement {

  def element: String = fields(Element)

  def withElement(element: String): this.type = set(Element, name)

  override def meta = DomainScalarExtensionModel
}

object DomainScalarExtension {
  def apply(): DomainExtension = apply(Annotations())

  def apply(ast: YPart): DomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): DomainExtension = new DomainScalarExtension(Fields(), annotations)
}
