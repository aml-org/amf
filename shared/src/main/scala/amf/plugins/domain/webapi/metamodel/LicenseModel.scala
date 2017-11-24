package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Iri, Str}
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.webapi.models.License
import amf.framework.vocabulary.Namespace.{Http, Schema}
import amf.framework.vocabulary.ValueType

/**
  * License metamodel
  */
object LicenseModel extends DomainElementModel {

  val Url = Field(Iri, Schema + "url")

  val Name = Field(Str, Schema + "name")

  override val `type`: List[ValueType] = Http + "License" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Name :: DomainElementModel.fields

  override def modelInstance = License()
}
