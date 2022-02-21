package amf.apiinstance.client.scala.model.domain

import amf.apiinstance.internal.metamodel.domain.policies.BasicAuthPolicyModel
import amf.apiinstance.internal.metamodel.domain.policies.BasicAuthPolicyModel.{Category, Namespace}
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.metamodel.Field

trait PolicyDomainElement { self: DomainElement with NamedDomainElement =>

  def namespace: StrField = fields.field(Namespace)
  def withNamespace(namespace: String): self.type = set(Namespace, namespace)

  def category: StrField = fields.field(Category)
  def withCategory(category: String): self.type = set(Category, category)

  override private[amf] def componentId = s"/${namespace.option().getOrElse("default")}_${name.value()}"
  override protected def nameField: Field = BasicAuthPolicyModel.Name
}
