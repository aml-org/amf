package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Iri, Str}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Shacl
import amf.vocabulary.ValueType

object SchemaShapeModel extends ShapeModel with DomainElementModel {
  val MediaType = Field(Str, Shacl + "mediaType")
  val Raw       = Field(Str, Shacl + "raw")

  override val fields = List(MediaType, Raw) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "SchemaShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}
