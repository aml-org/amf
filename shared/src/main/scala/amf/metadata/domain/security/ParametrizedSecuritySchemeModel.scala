package amf.metadata.domain.security

import amf.metadata.Type.{Iri, Str}
import amf.metadata.domain.DomainElementModel
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

object ParametrizedSecuritySchemeModel extends DomainElementModel {

  val Name = Field(Str, Security + "name")

  val Scheme = Field(Iri, Security + "scheme")

  val Settings = Field(SettingsModel, Security + "settings")

  override def fields: List[Field] = List(Name, Scheme, Settings) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "ParametrizedSecurityScheme") ++ DomainElementModel.`type`
}
