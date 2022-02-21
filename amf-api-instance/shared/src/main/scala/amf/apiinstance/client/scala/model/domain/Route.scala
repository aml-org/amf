package amf.apiinstance.client.scala.model.domain

import amf.apiinstance.internal.metamodel.domain.{FilterChainModel, RouteModel}
import amf.apiinstance.internal.metamodel.domain.RouteModel.{Path, Rule}
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class Route(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = RouteModel

  def rules: Seq[FilterRule] = fields.field(Rule)
  def withRules(rules: Seq[FilterRule]) = setArray(Rule, rules)
  def withRule(rule: FilterRule) = withRules(rules ++ Seq(rule))

  def path: StrField = fields.field(Path)
  def withPath(path: String) = set(Path, path)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = s"/route/${path}"
}

object Route {
  def apply(): Route = apply(Annotations())

  def apply(ast: YMap): Route = apply(Annotations(ast))

  def apply(node: YNode): Route = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): Route = Route(Fields(), annotations)
}