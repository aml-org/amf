package amf.domain.security

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.security.ScopeModel._

case class Scope(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String        = fields(Name)
  def description: String = fields(Description)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object Scope {

  def apply(): Scope = apply(Annotations())

  def apply(annotations: Annotations): Scope = new Scope(Fields(), annotations)
}
