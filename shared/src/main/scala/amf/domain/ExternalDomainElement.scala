package amf.domain

import amf.metadata.domain.ExternalDomainElementModel.Raw
import org.yaml.model.YMap

case class ExternalDomainElement(fields: Fields, annotations: Annotations) extends  DomainElement {

  def raw: String = fields(Raw)

  def withRaw(text: String) = set(Raw, text)

  override def adopted(parent: String) = withId(parent + "#/external")
}

object ExternalDomainElement {

  def apply(): ExternalDomainElement = apply(Annotations())

  def apply(ast: YMap): ExternalDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): ExternalDomainElement = ExternalDomainElement(Fields(), annotations)
}