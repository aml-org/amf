package amf.apicontract.internal.metamodel.domain.security

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.internal.metamodel.domain.{ParametersFieldModel, ResponseModel}
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core, Security}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}

object SecuritySchemeModel
    extends DomainElementModel
    with KeyField
    with DescriptionField
    with DisplayNameField
    with ParametersFieldModel {

  val Name = Field(
    Str,
    Core + "name",
    ModelDoc(ModelVocabularies.Core, "name", "Name for the security scheme", Seq((Namespace.Core + "name").iri()))
  )

  val Type = Field(Str, Security + "type", ModelDoc(ModelVocabularies.Security, "type", "Type of security scheme"))

  val Responses = Field(
    Array(ResponseModel),
    ApiContract + "response",
    ModelDoc(ModelVocabularies.ApiContract, "response", "Response associated to this security scheme")
  )

  val Settings = Field(
    SettingsModel,
    Security + "settings",
    ModelDoc(ModelVocabularies.Security, "settings", "Security scheme settings")
  )

  override val key: Field = Name

  override val `type`: List[ValueType] = Security + "SecurityScheme" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(
      Name,
      Type,
      DisplayName,
      Description,
      Headers,
      QueryParameters,
      Responses,
      Settings,
      QueryString
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = SecurityScheme()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "SecurityScheme",
    "Authentication and access control mechanism defined in an API"
  )
}
