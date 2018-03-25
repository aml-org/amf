package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.vocabulary.Namespace.{Http, Schema}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Server

/**
  * Server meta model
  */
object ServerModel extends DomainElementModel {
  val Url = Field(Str, Http + "url")

  val Description = Field(Str, Schema + "description")

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
