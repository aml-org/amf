package amf.metadata.domain.security

import amf.metadata.Field
import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.{DomainElementModel, ParameterModel, ResponseModel}
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

object SecuritySchemeModel extends DomainElementModel {
  val Name = Field(Str, Security + "name")

  val Type = Field(Str, Security + "type")

  val DisplayName = Field(Str, Security + "displayName")

  val Description = Field(Str, Security + "description")

  val Headers = Field(Array(ParameterModel), Security + "header")

  val QueryParameters = Field(Array(ParameterModel), Security + "parameter")

  val Responses = Field(Array(ResponseModel), Security + "response")

  val Settings = Field(SettingsModel, Security + "settings")

  override val `type`: List[ValueType] = Security + "SecurityScheme" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Type, DisplayName, Description, Headers, QueryParameters, Responses, Settings) ++ DomainElementModel.fields
}
