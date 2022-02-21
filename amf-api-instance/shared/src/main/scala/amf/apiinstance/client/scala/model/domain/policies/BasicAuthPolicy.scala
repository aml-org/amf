package amf.apiinstance.client.scala.model.domain.policies

import amf.apiinstance.client.scala.model.domain.PolicyDomainElement
import amf.apiinstance.internal.metamodel.domain.policies.BasicAuthPolicyModel
import amf.apiinstance.internal.metamodel.domain.policies.BasicAuthPolicyModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.metamodel.{Field, Obj}
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class BasicAuthPolicy(fields: Fields, annotations: Annotations) extends DomainElement with NamedDomainElement with PolicyDomainElement {

  override def meta: Obj = BasicAuthPolicyModel

  def username: StrField = fields.field(Username)
  def withUsername(username: String) = set(Username, username)

  def password: StrField = fields.field(Password)
  def withPassword(password: String) = set(Password, password)

}

object BasicAuthPolicy {
  def apply(): BasicAuthPolicy = apply(Annotations())

  def apply(ast: YMap): BasicAuthPolicy = apply(Annotations(ast))

  def apply(node: YNode): BasicAuthPolicy = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): BasicAuthPolicy = BasicAuthPolicy(Fields(), annotations)
    .withName("BasicAuth")
    .withCategory("Authentication")
}