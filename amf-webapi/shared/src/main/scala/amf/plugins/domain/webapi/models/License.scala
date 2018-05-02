package amf.plugins.domain.webapi.models

import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.LicenseModel
import amf.plugins.domain.webapi.metamodel.LicenseModel._
import org.yaml.model.YNode

/**
  * License internal model
  */
case class License(fields: Fields, annotations: Annotations) extends DomainElement {

  def url: StrField  = fields.field(Url)
  def name: StrField = fields.field(Name)

  def withUrl(url: String): this.type   = set(Url, url)
  def withName(name: String): this.type = set(Name, name)

  override def meta = LicenseModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/license"
}

object License {

  def apply(): License = apply(Annotations())

  def apply(node: YNode): License = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): License = new License(Fields(), annotations)
}
