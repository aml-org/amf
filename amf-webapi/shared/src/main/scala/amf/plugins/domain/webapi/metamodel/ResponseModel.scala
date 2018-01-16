package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.webapi.models.Response

/**
  * Response metamodel.
  */
object ResponseModel extends DomainElementModel with KeyField with OptionalField {

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
