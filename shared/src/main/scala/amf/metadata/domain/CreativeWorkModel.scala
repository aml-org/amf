package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type.Str
import amf.vocabulary.Namespace.Schema
import amf.vocabulary.ValueType

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends DomainElementModel {

  val Url = Field(Str, Schema + "url")

  val Description = Field(Str, Schema + "description")

  override val `type`: List[ValueType] = Schema + "CreativeWork" :: DomainElementModel.`type`
}
