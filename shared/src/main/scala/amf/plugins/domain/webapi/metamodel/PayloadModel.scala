package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.metadata.domain.{DomainElementModel, KeyField}
import amf.plugins.domain.shapes.metamodel.ShapeModel
import amf.plugins.domain.webapi.models.Payload
import amf.vocabulary.Namespace.Http
import amf.vocabulary.ValueType

/**
  * Payload metamodel.
  */
object PayloadModel extends DomainElementModel with KeyField {

  val MediaType = Field(Str, Http + "mediaType")

  val Schema = Field(ShapeModel, Http + "schema")

  override val key: Field = MediaType

  override val `type`: List[ValueType] = Http + "Payload" :: DomainElementModel.`type`

  override def fields: List[Field] = MediaType :: Schema :: DomainElementModel.fields

  override def modelInstance = Payload()
}
