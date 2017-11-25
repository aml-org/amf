package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.templates.KeyField
import amf.plugins.domain.shapes.metamodel.ShapeModel
import amf.plugins.domain.webapi.metamodel.{ParameterModel, ResponseModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.core.vocabulary.Namespace.Security
import amf.core.vocabulary.ValueType

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
