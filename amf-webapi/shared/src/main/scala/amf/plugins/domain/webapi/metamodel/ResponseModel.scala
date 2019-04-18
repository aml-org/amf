package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.common.ExamplesField
import amf.plugins.domain.webapi.models.Response

/**
  * Response metamodel.
  */
object ResponseModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with ExamplesField
    with LinkableElementModel
    with NameFieldSchema
    with DescriptionField
    with ParametersFieldModel {

  val StatusCode = Field(
    Str,
    ApiContract + "statusCode",
    ModelDoc(ModelVocabularies.ApiContract, "status code", "HTTP status code returned by a response"))

  val Payloads = Field(Array(PayloadModel),
                       ApiContract + "payload",
                       ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  val Links = Field(
    Array(TemplatedLinkModel),
    ApiContract + "link",
    ModelDoc(ModelVocabularies.ApiContract, "links", "Structural definition of links on the source data shape AST"))

  override val key: Field = StatusCode

  override val `type`: List[ValueType] = ApiContract + "Response" :: DomainElementModel.`type`

  override val fields: List[Field] =
    LinkableElementModel.fields ++
      List(Name, Description, StatusCode, Headers, Payloads, Links, Examples) ++ DomainElementModel.fields

  override def modelInstance = Response()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Response",
    "Response information for an operation"
  )
}
