package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Iri, Str}
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.webapi.models.Organization
import amf.framework.vocabulary.Namespace.Schema
import amf.framework.vocabulary.ValueType

/**
  * Organization metamodel
  */
object OrganizationModel extends DomainElementModel {

  val Url = Field(Iri, Schema + "url")

  val Name = Field(Str, Schema + "name")

  val Email = Field(Str, Schema + "email")

  override val `type`: List[ValueType] = Schema + "Organization" :: DomainElementModel.`type`

  override def fields: List[Field] = List(Url, Name, Email) ++ DomainElementModel.fields

  override def modelInstance = Organization()
}
