package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.OrganizationModel
import amf.apicontract.internal.metamodel.domain.OrganizationModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.YNode

/** Organization internal model
  */
case class Organization(fields: Fields, annotations: Annotations) extends NamedDomainElement {

  def url: StrField   = fields.field(Url)
  def email: StrField = fields.field(Email)

  def withUrl(url: String): this.type     = set(Url, url)
  def withEmail(email: String): this.type = set(Email, email)

  override def meta = OrganizationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/organization"

  override def nameField: Field = Name
}

object Organization {

  def apply(): Organization = apply(Annotations())

  def apply(node: YNode): Organization = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): Organization = apply(Fields(), annotations)
}
