package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type.Str
import amf.vocabulary.Namespace.Schema
import amf.vocabulary.ValueType

/**
  * Organization metamodel
  */
object OrganizationModel extends DomainElementModel {

  val Url = Field(Str, Schema + "url")

  val Name = Field(Str, Schema + "name")

  val Email = Field(Str, Schema + "email")

  override val `type`: List[ValueType] = Schema + "Organization" :: DomainElementModel.`type`

  override val fields: List[Field] = Url :: Name :: Email :: DomainElementModel.fields
}
