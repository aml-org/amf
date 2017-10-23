package amf.metadata.domain.security

import amf.metadata.Field
import amf.metadata.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

object ScopeModel extends DomainElementModel {

  val Name = Field(Str, Security + "name")

  val Description = Field(Str, Security + "description")

  override def fields: List[Field] = List(Name, Description) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "Scope")
}
