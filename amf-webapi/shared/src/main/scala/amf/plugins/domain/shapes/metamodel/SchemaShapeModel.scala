package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.plugins.domain.shapes.models.SchemaShape
import amf.core.vocabulary.Namespace.Shacl
import amf.core.vocabulary.ValueType

object SchemaShapeModel extends AnyShapeModel {
  val MediaType = Field(Str, Shacl + "mediaType")
  val Raw       = Field(Str, Shacl + "raw")

  override val fields = List(MediaType, Raw) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "SchemaShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = SchemaShape()
}
