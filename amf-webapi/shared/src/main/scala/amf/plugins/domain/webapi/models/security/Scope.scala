package amf.plugins.domain.webapi.models.security

import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.security.ScopeModel
import amf.plugins.domain.webapi.metamodel.security.ScopeModel._
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

  override def meta = ScopeModel
}

object Scope {

  def apply(): Scope = apply(Annotations())

  def apply(part: YPart): Scope = apply(Annotations(part))

  def apply(annotations: Annotations): Scope = new Scope(Fields(), annotations)
}
