package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.templates.KeyField
import amf.core.vocabulary.Namespace.Http
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Encoding

/**
  * Encoding metamodel.
  */
object EncodingModel extends DomainElementModel with KeyField {

  val PropertyName = Field(Str, Http + "propertyName")

  val ContentType = Field(Str, Http + "contentType")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val Style = Field(Str, Http + "style")

  val Explode = Field(Bool, Http + "explode")

  val AllowReserved = Field(Bool, Http + "allowReserved")

  override val `type`: List[ValueType] = Http + "Encoding" :: DomainElementModel.`type`

  override def fields: List[Field] =
    PropertyName :: ContentType :: Headers :: Style :: Explode :: AllowReserved :: DomainElementModel.fields

  override def modelInstance = Encoding()

  override val key: Field = PropertyName
}
