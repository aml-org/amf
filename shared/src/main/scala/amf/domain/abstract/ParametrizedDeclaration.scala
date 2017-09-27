package amf.domain.`abstract`

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.`abstract`.ParametrizedDeclarationModel._
import org.yaml.model.YPart

abstract class ParametrizedDeclaration(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String                  = fields(Name)
  def target: String                = fields(Target)
  def variables: Seq[VariableValue] = fields(Variables)

  def withName(name: String): this.type                       = set(Name, name)
  def withTarget(target: String): this.type                   = set(Target, target)
  def withVariables(variables: Seq[VariableValue]): this.type = setArray(Variables, variables)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

case class ParametrizedResourceType(fields: Fields, annotations: Annotations)
    extends ParametrizedDeclaration(fields, annotations)

object ParametrizedResourceType {
  def apply(): ParametrizedResourceType = apply(Annotations())

  def apply(ast: YPart): ParametrizedResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ParametrizedResourceType = ParametrizedResourceType(Fields(), annotations)
}

case class ParametrizedTrait(fields: Fields, annotations: Annotations)
    extends ParametrizedDeclaration(fields, annotations)

object ParametrizedTrait {
  def apply(): ParametrizedTrait = apply(Annotations())

  def apply(ast: YPart): ParametrizedTrait = apply(Annotations(ast))

  def apply(annotations: Annotations): ParametrizedTrait = ParametrizedTrait(Fields(), annotations)
}
