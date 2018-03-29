package amf.core.model.domain

import amf.client.model.StrField
import amf.core.metamodel.domain.ExternalDomainElementModel
import amf.core.metamodel.domain.ExternalDomainElementModel.{MediaType, Raw}
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YMap

case class ExternalDomainElement(fields: Fields, annotations: Annotations) extends DomainElement {

  def raw: StrField       = fields.field(Raw)
  def mediaType: StrField = fields.field(MediaType)

  def withRaw(text: String): this.type            = set(Raw, text)
  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)

  override def meta: ExternalDomainElementModel.type = ExternalDomainElementModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "#/external"
}

object ExternalDomainElement {

  def apply(): ExternalDomainElement = apply(Annotations())

  def apply(ast: YMap): ExternalDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): ExternalDomainElement = ExternalDomainElement(Fields(), annotations)
}
