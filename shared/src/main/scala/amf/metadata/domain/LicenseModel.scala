package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type.{Iri, Str}
import amf.vocabulary.Namespace.{Http, Schema}
import amf.vocabulary.ValueType

/**
  * License metamodel
  */
object LicenseModel extends DomainElementModel {

  val Url = Field(Iri, Schema + "url")

  val Name = Field(Str, Schema + "name")

  override val `type`: List[ValueType] = Http + "License" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Name :: DomainElementModel.fields
}
