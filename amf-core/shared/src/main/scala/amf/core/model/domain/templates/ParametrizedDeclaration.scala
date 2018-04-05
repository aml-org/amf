package amf.core.model.domain.templates

import amf.core.metamodel.domain.templates.ParametrizedDeclarationModel._
import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}

abstract class ParametrizedDeclaration(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField                = fields.field(Name)
  def target: AbstractDeclaration   = fields.field(Target)
  def variables: Seq[VariableValue] = fields.field(Variables)

  def withName(name: String): this.type                       = set(Name, name)
  def withTarget(target: AbstractDeclaration): this.type      = set(Target, target)
  def withVariables(variables: Seq[VariableValue]): this.type = setArray(Variables, variables)

  override def componentId: String = "/" + name.value()
}
