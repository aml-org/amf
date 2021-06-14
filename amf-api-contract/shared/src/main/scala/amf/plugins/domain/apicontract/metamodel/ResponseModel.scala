package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.scala.client.vocabulary.Namespace._
import amf.core.scala.client.vocabulary.ValueType
import amf.plugins.domain.apicontract.models.Response

/**
  * Response metamodel.
  */
object ResponseModel extends DomainElementModel with KeyField with OptionalField with MessageModel {

  val StatusCode = Field(
    Str,
    ApiContract + "statusCode",
    ModelDoc(ModelVocabularies.ApiContract, "statusCode", "HTTP status code returned by a response"))

  val Links = Field(
    Array(TemplatedLinkModel),
    ApiContract + "link",
    ModelDoc(ModelVocabularies.ApiContract, "links", "Structural definition of links on the source data shape AST")
  )

  override val key: Field = StatusCode

  override val `type`: List[ValueType] = ApiContract + "Response" :: MessageModel.`type`

  override val fields: List[Field] =
    List(StatusCode, Links) ++ MessageModel.fields

  override def modelInstance = Response()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Response",
    "Response information for an operation"
  )
}
