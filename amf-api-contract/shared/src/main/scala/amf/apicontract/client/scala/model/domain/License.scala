package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.LicenseModel
import amf.apicontract.internal.metamodel.domain.LicenseModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.YNode

/**
  * License internal model
  */
case class License(fields: Fields, annotations: Annotations) extends NamedDomainElement {

  def url: StrField = fields.field(Url)

  def withUrl(url: String): this.type = set(Url, url)

  override def meta = LicenseModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/license"

  override def nameField: Field = Name
}

object License {

  def apply(): License = apply(Annotations())

  def apply(node: YNode): License = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): License = new License(Fields(), annotations)
}
