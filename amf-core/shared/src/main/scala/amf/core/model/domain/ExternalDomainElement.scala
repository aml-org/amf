package amf.core.model.domain

import amf.core.metamodel.domain.ExternalDomainElementModel
import amf.core.metamodel.domain.ExternalDomainElementModel.{MediaType, Raw}
import amf.core.model.StrField
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class ExternalDomainElement(fields: Fields, annotations: Annotations) extends DomainElement {

  def raw: StrField       = fields.field(Raw)
  def mediaType: StrField = fields.field(MediaType)

  def withRaw(text: String): this.type            = set(Raw, text)
  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)

  override def meta: ExternalDomainElementModel.type = ExternalDomainElementModel

  // temporal cache for the parsed data from external fragments
  var parsed: Option[YNode] = None

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "#/external"
}

object ExternalDomainElement {

  def apply(): ExternalDomainElement = apply(Annotations())

  def apply(ast: YMap): ExternalDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): ExternalDomainElement = ExternalDomainElement(Fields(), annotations)
}
