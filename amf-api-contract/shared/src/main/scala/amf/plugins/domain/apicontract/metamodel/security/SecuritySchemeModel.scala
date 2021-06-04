package amf.plugins.domain.apicontract.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.templates.KeyField
import amf.plugins.domain.apicontract.metamodel.{ParameterModel, ParametersFieldModel, ResponseModel}
import amf.plugins.domain.apicontract.models.security.SecurityScheme
import amf.core.vocabulary.Namespace.{ApiContract, Core, Security}
import amf.core.vocabulary.{Namespace, ValueType}

object SecuritySchemeModel
    extends DomainElementModel
    with KeyField
    with DescriptionField
    with DisplayNameField
    with ParametersFieldModel {

  val Name = Field(
    Str,
    Core + "name",
    ModelDoc(ModelVocabularies.Core, "name", "Name for the security scheme", Seq((Namespace.Core + "name").iri())))

  val Type = Field(Str, Security + "type", ModelDoc(ModelVocabularies.Security, "type", "Type of security scheme"))

  val Responses = Field(
    Array(ResponseModel),
    ApiContract + "response",
    ModelDoc(ModelVocabularies.ApiContract, "response", "Response associated to this security scheme"))

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
    "SecurityScheme",
    "Authentication and access control mechanism defined in an API"
  )
}
