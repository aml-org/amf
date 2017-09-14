package amf.domain.extensions

import amf.common.AMFAST
import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.extensions.CustomDomainPropertyModel.{Description, Domain, Name, Schema}
import amf.shape.Shape
import amf.common.core._

case class CustomDomainProperty(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String        = fields(Name)
  def description: String = fields(Description)
  def domain: Seq[String] = fields(Domain)
  def schema: Shape       = fields(Schema)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withDomain(domain: Seq[String]): this.type      = set(Domain, domain)
  def withSchema(schema: Shape): this.type            = set(Schema, schema)

  override def adopted(parent: String) = withId(parent + "/" + name.urlEncoded)
}

object CustomDomainProperty {
  def apply(): CustomDomainProperty = apply(Annotations())

  def apply(ast: AMFAST): CustomDomainProperty = apply(Annotations(ast))

  def apply(annotations: Annotations): CustomDomainProperty = CustomDomainProperty(Fields(), annotations)
}
