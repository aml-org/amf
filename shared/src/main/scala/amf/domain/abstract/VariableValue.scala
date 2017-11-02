package amf.domain.`abstract`

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.`abstract`.VariableValueModel
import org.yaml.model.YPart

case class VariableValue(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String  = fields(VariableValueModel.Name)
  def value: String = fields(VariableValueModel.Value)

  def withName(name: String): this.type   = set(VariableValueModel.Name, name)
  def withValue(value: String): this.type = set(VariableValueModel.Value, value)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object VariableValue {

  def apply(): VariableValue = apply(Annotations())

  def apply(ast: YPart): VariableValue = apply(Annotations(ast))

  def apply(annotations: Annotations): VariableValue = apply(Fields(), annotations)
}

case class Variable(name: String, value: String)
