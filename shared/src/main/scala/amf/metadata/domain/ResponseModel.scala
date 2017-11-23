package amf.metadata.domain
import amf.domain.Response
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Str}
import amf.vocabulary.Namespace._
import amf.vocabulary.ValueType

/**
  * Response metamodel.
  */
object ResponseModel extends DomainElementModel with KeyField {

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val StatusCode = Field(Str, Hydra + "statusCode")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val Payloads = Field(Array(PayloadModel), Http + "payload")

  val Examples = Field(Array(ExampleModel), Document + "examples")

  override val key: Field = StatusCode

  override val `type`: List[ValueType] = Http + "Response" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Description, StatusCode, Headers, Payloads, Examples) ++ DomainElementModel.fields

  override def modelInstance = Response()
}
