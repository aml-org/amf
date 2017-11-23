package amf.domain.`abstract`

import amf.domain.Fields
import amf.framework.model.domain.DomainElement
import amf.framework.parser.Annotations
import amf.metadata.domain.`abstract`.ParametrizedDeclarationModel._
import amf.metadata.domain.`abstract`.{ParametrizedResourceTypeModel, ParametrizedTraitModel}
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
    extends ParametrizedDeclaration(fields, annotations) {
  override def meta = ParametrizedResourceTypeModel
}

object ParametrizedResourceType {
  def apply(): ParametrizedResourceType = apply(Annotations())

  def apply(ast: YPart): ParametrizedResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ParametrizedResourceType = ParametrizedResourceType(Fields(), annotations)
}

case class ParametrizedTrait(fields: Fields, annotations: Annotations)
    extends ParametrizedDeclaration(fields, annotations) {
  override def meta = ParametrizedTraitModel
}

object ParametrizedTrait {
  def apply(): ParametrizedTrait = apply(Annotations())

  def apply(ast: YPart): ParametrizedTrait = apply(Annotations(ast))

  def apply(annotations: Annotations): ParametrizedTrait = ParametrizedTrait(Fields(), annotations)
}
