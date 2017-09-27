package amf.domain.`abstract`

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.`abstract`.VariableModel._
import org.yaml.model.YPart

case class Variable(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String           = fields(Name)
  def transformation: String = fields(Transformation)

  def withName(name: String): this.type                     = set(Name, name)
  def withTransformation(transformation: String): this.type = set(Transformation, transformation)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object Variable {

  def apply(): Variable = apply(Annotations())

  def apply(ast: YPart): Variable = apply(Annotations(ast))

  def apply(annotations: Annotations): Variable = apply(Fields(), annotations)
}
