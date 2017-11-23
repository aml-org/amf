package amf.metadata.domain.security

import amf.domain.security.Scope
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

object ScopeModel extends DomainElementModel {

  val Name = Field(Str, Security + "name")

  val Description = Field(Str, Security + "description")

  override def fields: List[Field] = List(Name, Description) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "Scope") ++ DomainElementModel.`type`

  override def modelInstance = Scope()
}
