package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.templates.KeyField
import amf.plugins.domain.webapi.metamodel.{ParameterModel, ParametersFieldModel, ResponseModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.core.vocabulary.Namespace.{Http, Hydra, Schema, Security}
import amf.core.vocabulary.{Namespace, ValueType}

object SecuritySchemeModel
    extends DomainElementModel
    with KeyField
    with DescriptionField
    with DisplayNameField
    with ParametersFieldModel {

  val Name = Field(Str,
                   Security + "name",
                   ModelDoc(ModelVocabularies.Security,
                            "name",
                            "Name for the security scheme",
                            Seq((Namespace.Schema + "name").iri())))

  val Type = Field(Str, Security + "type", ModelDoc(ModelVocabularies.Security, "type", "Type of security scheme"))

  val Responses = Field(
    Array(ResponseModel),
    Hydra + "response",
    ModelDoc(ExternalModelVocabularies.Hydra, "response", "Response associated to this security scheme"))

  val Settings = Field(SettingsModel,
                       Security + "settings",
                       ModelDoc(ModelVocabularies.Security, "settings", "Security scheme settings"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Security + "SecurityScheme" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(Name, Type, DisplayName, Description, Headers, QueryParameters, Responses, Settings, QueryString) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = SecurityScheme()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Security Scheme",
    "Authentication and access control mechanism defined in an API"
  )
}
