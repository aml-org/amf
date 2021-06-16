package amf.apicontract.internal.metamodel.domain.security

import amf.apicontract.client.scala.model.domain.security.ParametrizedSecurityScheme
import amf.core.client.scala.vocabulary.Namespace.{Core, Security}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object ParametrizedSecuritySchemeModel extends DomainElementModel with KeyField {

  val Name = Field(
    Str,
    Core + "name",
    ModelDoc(ModelVocabularies.Core, "name", "Name for the security scheme", Seq((Namespace.Core + "name").iri())))

  val Description = Field(
    Str,
    Namespace.Core + "description",
    ModelDoc(ModelVocabularies.Security,
             "description",
             "Name for the security scheme",
             Seq((Namespace.Core + "description").iri()))
  )

  val Scheme = Field(SecuritySchemeModel, Security + "scheme", ModelDoc(ModelVocabularies.Security, "scheme", ""))

  val Settings = Field(SettingsModel,
                       Security + "settings",
                       ModelDoc(ModelVocabularies.Security, "settings", "Security scheme settings"))

  override val key: Field = Name

  override def fields: List[Field] = List(Name, Scheme, Settings) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "ParametrizedSecurityScheme") ++ DomainElementModel.`type`

  override def modelInstance = ParametrizedSecurityScheme()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "ParametrizedSecurityScheme",
    ""
  )
}
