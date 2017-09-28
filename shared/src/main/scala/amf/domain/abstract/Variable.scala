package amf.domain.`abstract`

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.`abstract`.{VariableModel, VariableValueModel}
import org.yaml.model.YPart

case class Variable(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String           = fields(VariableModel.Name)
  def transformation: String = fields(VariableModel.Transformation)

  def withName(name: String): this.type                     = set(VariableModel.Name, name)
  def withTransformation(transformation: String): this.type = set(VariableModel.Transformation, transformation)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object Variable {

  def apply(): Variable = apply(Annotations())

  def apply(ast: YPart): Variable = apply(Annotations(ast))

  def apply(annotations: Annotations): Variable = apply(Fields(), annotations)
}

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
