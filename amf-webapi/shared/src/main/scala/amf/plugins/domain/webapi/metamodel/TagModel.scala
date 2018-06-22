package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.core.vocabulary.Namespace.{Http, Schema}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.models.Tag

/**
  * Tag meta model
  */
object TagModel extends DomainElementModel {
  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Documentation = Field(CreativeWorkModel, Schema + "externalDocs")

  override val `type`: List[ValueType] = Http + "Tag" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Name,
      Description,
      Documentation
    ) ++ DomainElementModel.fields

  override def modelInstance = Tag()
}