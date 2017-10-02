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

  val UriParameters = Field(Array(ParameterModel), Http + "parameter")

  override val `type`: List[ValueType] = Http + "EndPoint" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Path, Name, Description, Operations, UriParameters) ++ DomainElementModel.fields
}
