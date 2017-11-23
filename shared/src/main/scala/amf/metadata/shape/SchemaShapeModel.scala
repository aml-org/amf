package amf.metadata.shape

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Iri, Str}
import amf.metadata.domain.DomainElementModel
import amf.shape.SchemaShape
import amf.vocabulary.Namespace.Shacl
import amf.vocabulary.ValueType

object SchemaShapeModel extends ShapeModel with DomainElementModel {
  val MediaType = Field(Str, Shacl + "mediaType")
  val Raw       = Field(Str, Shacl + "raw")

  override val fields = List(MediaType, Raw) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "SchemaShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = SchemaShape()
}
