package amf.core.model.domain.templates

import amf.core.metamodel.domain.templates.VariableValueModel
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class VariableValue(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String  = fields(VariableValueModel.Name)
  def value: String = fields(VariableValueModel.Value)

  def withName(name: String): this.type   = set(VariableValueModel.Name, name)
  def withValue(value: String): this.type = set(VariableValueModel.Value, value)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)

  override def meta = VariableValueModel
}

object VariableValue {

  def apply(): VariableValue = apply(Annotations())

  def apply(ast: YPart): VariableValue = apply(Annotations(ast))

  def apply(annotations: Annotations): VariableValue = apply(Fields(), annotations)
}

case class Variable(name: String, value: String)
