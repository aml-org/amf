package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type.{Iri, Str}
import amf.vocabulary.Namespace.Schema
import amf.vocabulary.ValueType

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends DomainElementModel {

  val Url = Field(Iri, Schema + "url")

  val Description = Field(Str, Schema + "description")

  override val `type`: List[ValueType] = Schema + "CreativeWork" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Description :: DomainElementModel.fields
}
