package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.webapi.models.CreativeWork
import amf.core.vocabulary.Namespace.Schema
import amf.core.vocabulary.ValueType

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends DomainElementModel {

  val Url = Field(Iri, Schema + "url")

  val Title = Field(Str, Schema + "title")

  val Description = Field(Str, Schema + "description")

  override val `type`: List[ValueType] = Schema + "CreativeWork" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Title :: Description :: DomainElementModel.fields

  override def modelInstance = CreativeWork()
}
