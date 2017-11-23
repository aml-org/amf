package amf.metadata.domain
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.metadata.shape.ShapeModel
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
}
