package amf.metadata.domain
import amf.metadata.Field
import amf.metadata.Type.Str
import amf.vocabulary.Namespace.Http
import amf.vocabulary.ValueType

/**
  * Payload metamodel.
  */
object PayloadModel extends DomainElementModel {

  val MediaType = Field(Str, Http + "mediaType")

  val Schema = Field(Str, Http + "schema") //TODO this is a shacl:Shape

  override val `type`: List[ValueType] = Http + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] = MediaType :: Schema :: DomainElementModel.fields
}
