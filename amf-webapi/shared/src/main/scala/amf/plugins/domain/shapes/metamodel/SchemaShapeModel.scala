package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DomainElementModel, ExternalSourceElementModel, LinkableElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.Shacl
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.SchemaShape

object SchemaShapeModel extends AnyShapeModel with ExternalSourceElementModel {
  val MediaType = Field(Str, Shacl + "mediaType")

  override val fields = List(MediaType) ++
    AnyShapeModel.fields ++
    DomainElementModel.fields ++
    LinkableElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "SchemaShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = SchemaShape()

  override val dynamic = true
}