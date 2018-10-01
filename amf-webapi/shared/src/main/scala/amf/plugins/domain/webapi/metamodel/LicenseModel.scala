package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.plugins.domain.webapi.models.License
import amf.core.vocabulary.Namespace.{Http, Schema}
import amf.core.vocabulary.ValueType

/**
  * License metamodel
  *
  * License for the API
  */
object LicenseModel extends DomainElementModel with NameFieldSchema {

  val Url = Field(Iri, Schema + "url")

  override val `type`: List[ValueType] = Http + "License" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Name :: DomainElementModel.fields

  override def modelInstance = License()
}
