package amf.metadata.domain.security

import amf.domain.security.SecurityScheme
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Str}
import amf.metadata.domain.{DomainElementModel, KeyField, ParameterModel, ResponseModel}
import amf.metadata.shape.ShapeModel
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

object SecuritySchemeModel extends DomainElementModel with KeyField {

  val Name = Field(Str, Security + "name")

  val Type = Field(Str, Security + "type")

  val DisplayName = Field(Str, Security + "displayName")

  val Description = Field(Str, Security + "description")

  val Headers = Field(Array(ParameterModel), Security + "header")

  val QueryParameters = Field(Array(ParameterModel), Security + "parameter")

  val Responses = Field(Array(ResponseModel), Security + "response")

  val Settings = Field(SettingsModel, Security + "settings")

  val QueryString = Field(ShapeModel, Security + "queryString")

  override val key: Field = Name

  override val `type`: List[ValueType] = Security + "SecurityScheme" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Type, DisplayName, Description, Headers, QueryParameters, Responses, Settings, QueryString) ++ DomainElementModel.fields

  override def modelInstance = SecurityScheme()
}
