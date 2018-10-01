package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.vocabulary.Namespace.Http
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Server

/**
  * Server meta model
  */
object ServerModel extends DomainElementModel with DescriptionField {
  val Url = Field(Str, Http + "url")

  val Variables = Field(Array(ParameterModel), Http + "variable")

  override val `type`: List[ValueType] = Http + "Server" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Url,
      Description,
      Variables
    ) ++ DomainElementModel.fields

  override def modelInstance = Server()
}
