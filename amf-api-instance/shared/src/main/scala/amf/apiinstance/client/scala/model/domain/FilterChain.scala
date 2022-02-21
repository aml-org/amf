package amf.apiinstance.client.scala.model.domain

import amf.apiinstance.internal.metamodel.domain.FilterChainModel
import amf.apiinstance.internal.metamodel.domain.FilterChainModel.{Policies, Rule}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class FilterChain(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = FilterChainModel

  def rules: Seq[FilterRule] = fields.field(Rule)
  def withRules(rules: Seq[FilterRule]) = setArray(Rule, rules)
  def withRule(rule: FilterRule) = withRules(rules ++ Seq(rule))

  def policies: Seq[PolicyDomainElement] = fields.field(Policies)
  def withPolicies(policies: Seq[PolicyDomainElement]) = setArray(Policies, policies.map(_.asInstanceOf[DomainElement]))
  def withPolicy(policy: PolicyDomainElement) = withPolicies(policies ++ Seq(policy))

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = "/chain"

}

object FilterChain {
  def apply(): FilterChain = apply(Annotations())

  def apply(ast: YMap): FilterChain = apply(Annotations(ast))

  def apply(node: YNode): FilterChain = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): FilterChain = FilterChain(Fields(), annotations)
}