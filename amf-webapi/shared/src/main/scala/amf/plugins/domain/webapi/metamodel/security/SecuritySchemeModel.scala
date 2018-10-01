package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ShapeModel}
import amf.core.metamodel.domain.templates.KeyField
import amf.plugins.domain.webapi.metamodel.{ParameterModel, ResponseModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.core.vocabulary.Namespace.{Http, Hydra, Schema, Security}
import amf.core.vocabulary.ValueType

object SecuritySchemeModel extends DomainElementModel with KeyField with DescriptionField with DisplayNameField {

  val Name = Field(Str, Security + "name")

  val Type = Field(Str, Security + "type")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val QueryParameters = Field(Array(ParameterModel), Http + "parameter")

  val Responses = Field(Array(ResponseModel), Hydra + "response")

  val Settings = Field(SettingsModel, Security + "settings")

  val QueryString = Field(ShapeModel, Http + "queryString")

  override val key: Field = Name

  override val `type`: List[ValueType] = Security + "SecurityScheme" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Type, DisplayName, Description, Headers, QueryParameters, Responses, Settings, QueryString) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = SecurityScheme()
}
