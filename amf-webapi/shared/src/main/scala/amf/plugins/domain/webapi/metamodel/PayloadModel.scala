package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.Http
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.webapi.models.Payload

/**
  * Payload metamodel.
  */
object PayloadModel extends DomainElementModel with KeyField with OptionalField with LinkableElementModel {

  val MediaType = Field(Str, Http + "mediaType")

  val Schema = Field(ShapeModel, Http + "schema")

  val Examples = Field(Array(ExampleModel), Http + "example")

  val Encoding = Field(Array(EncodingModel), Http + "encoding")

  override val key: Field = MediaType

  override val `type`: List[ValueType] = Http + "Payload" :: DomainElementModel.`type`

  override def fields: List[Field] =
    MediaType :: Schema :: Examples :: Encoding :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = Payload()
}
