package amf.plugins.domain.webapi.models

import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.ExternalDomainElementModel
import amf.plugins.domain.webapi.metamodel.ExternalDomainElementModel._
import org.yaml.model.YMap

case class ExternalDomainElement(fields: Fields, annotations: Annotations) extends DomainElement {

  def raw: String = fields(Raw)

  def withRaw(text: String) = set(Raw, text)

  override def adopted(parent: String) = withId(parent + "#/external")

  override def meta = ExternalDomainElementModel
}

object ExternalDomainElement {

  def apply(): ExternalDomainElement = apply(Annotations())

  def apply(ast: YMap): ExternalDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): ExternalDomainElement = ExternalDomainElement(Fields(), annotations)
}
