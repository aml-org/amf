package amf.metadata.domain
import amf.metadata.{Field, Type}
import amf.metadata.Type.{Array, Str}
import amf.vocabulary.Namespace.{Http, Hydra, Schema}
import amf.vocabulary.ValueType

/**
  * Response metamodel.
  */
object ResponseModel extends DomainElementModel {

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val StatusCode = Field(Str, Hydra + "statusCode")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val Payloads = Field(Array(PayloadModel), Http + "payload")

  override val `type`: List[ValueType] = Http + "Response" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Description, StatusCode, Headers, Payloads) ++ DomainElementModel.fields
}
