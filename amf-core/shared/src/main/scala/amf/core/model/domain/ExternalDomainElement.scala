package amf.core.model.domain

import amf.core.metamodel.domain.ExternalDomainElementModel
import amf.core.metamodel.domain.ExternalDomainElementModel.{MediaType, Raw}
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YMap

case class ExternalDomainElement(fields: Fields, annotations: Annotations) extends DomainElement {

  def raw: String       = fields(Raw)
  def mediaType: String = fields(MediaType)

  def withRaw(text: String): this.type            = set(Raw, text)
  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)

  override def adopted(parent: String): this.type = withId(parent + "#/external")

  override def meta: ExternalDomainElementModel.type = ExternalDomainElementModel
}

object ExternalDomainElement {

  def apply(): ExternalDomainElement = apply(Annotations())

  def apply(ast: YMap): ExternalDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): ExternalDomainElement = ExternalDomainElement(Fields(), annotations)
}
