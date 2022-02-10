package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{ApiFederation, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.federation.FederatedAttribute
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapeOperation

// TODO: unify with api model:
// Option 1: move part of Operation model to a generic common operation (with request, response, parameters) to core. ApiOperation on api-contract to inherits of that
// Option 2: move all model to amf-model new package. amf-model will contain all objects (including shapes? at least any shape will be needed) That module can be before amf-shapes or event before amf-aml solving the problem of json schema.

object ShapeOperationModel
    extends FederatedAttribute
    with KeyField
    with OptionalField
    with NameFieldSchema
    with DescriptionField {

  val Method: Field = Field(
    Str,
    Shapes + "method",
    ModelDoc(ModelVocabularies.ApiContract, "method", "Type of operation over a shape. It follows HTTP semantics"))

  val Request: Field = Field(
    ShapeRequestModel,
    Shapes + "expects",
    ModelDoc(ModelVocabularies.ApiContract, "expects", "Request information required by the operation"))

  val Response: Field = Field(
    ShapeResponseModel,
    Shapes + "returns",
    ModelDoc(ModelVocabularies.ApiContract, "returns", "Response data returned by the operation"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Shapes + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(
    Name,
    Description,
    Request,
    Response
  ) ++ FederatedAttribute.fields

  override def modelInstance = ShapeOperation()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Operation",
    "Action that can be executed over the data of a particular shape"
  )
}
