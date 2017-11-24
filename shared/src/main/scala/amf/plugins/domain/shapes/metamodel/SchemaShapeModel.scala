package amf.plugins.domain.shapes.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.SchemaShape
import amf.framework.vocabulary.Namespace.Shacl
import amf.framework.vocabulary.ValueType

object SchemaShapeModel extends ShapeModel with DomainElementModel {
  val MediaType = Field(Str, Shacl + "mediaType")
  val Raw       = Field(Str, Shacl + "raw")

  override val fields = List(MediaType, Raw) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "SchemaShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = SchemaShape()
}
