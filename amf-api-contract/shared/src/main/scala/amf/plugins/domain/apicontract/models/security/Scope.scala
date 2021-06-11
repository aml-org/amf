package amf.plugins.domain.apicontract.models.security

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.security.ScopeModel
import amf.plugins.domain.apicontract.metamodel.security.ScopeModel._
import org.yaml.model.YPart
import amf.core.utils.AmfStrings

case class Scope(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField        = fields.field(Name)
  def description: StrField = fields.field(Description)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)

  def cloneScope(): Scope = {
    val cloned = Scope(annotations)

    this.fields.foreach {
      case (f, v) => cloned.set(f, v.value, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta = ScopeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-scope").urlComponentEncoded
}

object Scope {

  def apply(): Scope = apply(Annotations())

  def apply(part: YPart): Scope = apply(Annotations(part))

  def apply(annotations: Annotations): Scope = new Scope(Fields(), annotations)
}
