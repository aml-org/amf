package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type._
import amf.vocabulary.Namespace.{Http, Hydra, Schema}
import amf.vocabulary.ValueType

/**
  * EndPoint metamodel
  */
object EndPointModel extends DomainElementModel {

  val Path = Field(RegExp, Http + "path")

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Operations = Field(Array(OperationModel), Hydra + "supportedOperation")

  override val `type`: List[ValueType] = Http + "EndPoint" :: DomainElementModel.`type`

  override val fields: List[Field] = Path :: Name :: Description :: Operations :: DomainElementModel.fields
}
