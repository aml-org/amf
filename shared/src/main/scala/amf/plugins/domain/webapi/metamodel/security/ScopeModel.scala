package amf.plugins.domain.webapi.metamodel.security

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.webapi.models.security.Scope
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

object ScopeModel extends DomainElementModel {

  val Name = Field(Str, Security + "name")

  val Description = Field(Str, Security + "description")

  override def fields: List[Field] = List(Name, Description) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "Scope") ++ DomainElementModel.`type`

  override def modelInstance = Scope()
}
