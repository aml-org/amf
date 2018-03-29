package amf.plugins.domain.webapi.models

import amf.client.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.OrganizationModel
import amf.plugins.domain.webapi.metamodel.OrganizationModel._
import org.yaml.model.YMap

/**
  * Organization internal model
  */
case class Organization(fields: Fields, annotations: Annotations) extends DomainElement {

  def url: StrField   = fields.field(Url)
  def name: StrField  = fields.field(Name)
  def email: StrField = fields.field(Email)

  def withUrl(url: String): this.type     = set(Url, url)
  def withName(name: String): this.type   = set(Name, name)
  def withEmail(email: String): this.type = set(Email, email)

  override def meta = OrganizationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/organization"
}

object Organization {

  def apply(): Organization = apply(Annotations())

  def apply(ast: YMap): Organization = apply(Annotations(ast))

  def apply(annotations: Annotations): Organization = apply(Fields(), annotations)
}
