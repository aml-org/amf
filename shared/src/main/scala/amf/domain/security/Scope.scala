package amf.domain.security

import amf.domain.{DomainElement, Fields}
import amf.framework.parser.Annotations
import amf.metadata.domain.security.ScopeModel._
import org.yaml.model.YPart

case class Scope(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String        = fields(Name)
  def description: String = fields(Description)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)

  def cloneScope(): Scope = {
    val cloned = Scope(annotations)

    this.fields.foreach {
      case (f, v) => cloned.set(f, v.value, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }
}

object Scope {

  def apply(): Scope = apply(Annotations())

  def apply(part: YPart): Scope = apply(Annotations(part))

  def apply(annotations: Annotations): Scope = new Scope(Fields(), annotations)
}
