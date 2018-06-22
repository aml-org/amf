package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.core.vocabulary.Namespace.Schema
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.CreativeWork

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends DomainElementModel with LinkableElementModel {

  val Url = Field(Iri, Schema + "url")

  val Title = Field(Str, Schema + "title")

  val Description = Field(Str, Schema + "description")

  override val `type`: List[ValueType] = Schema + "CreativeWork" :: DomainElementModel.`type`

  override def fields: List[Field] =
    Url :: Title :: Description :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = CreativeWork()
}
