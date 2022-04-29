package amf.apicontract.client.scala.model.domain.security

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.security.ScopeModel
import amf.apicontract.internal.metamodel.domain.security.ScopeModel._
import org.yaml.model.YPart
import amf.core.internal.utils.AmfStrings

case class Scope(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField        = fields.field(Name)
  def description: StrField = fields.field(Description)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)

  def cloneScope(): Scope = {
    val cloned = Scope(annotations)

    this.fields.foreach { case (f, v) =>
      cloned.set(f, v.value, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta = ScopeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/" + name.option().getOrElse("default-scope").urlComponentEncoded
}

object Scope {

  def apply(): Scope = apply(Annotations())

  def apply(part: YPart): Scope = apply(Annotations(part))

  def apply(annotations: Annotations): Scope = new Scope(Fields(), annotations)
}
