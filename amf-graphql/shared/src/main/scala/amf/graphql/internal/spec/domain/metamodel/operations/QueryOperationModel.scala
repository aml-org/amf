package amf.graphql.internal.spec.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Core, Query, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.graphql.client.scala.model.domain.QueryOperation
import amf.shapes.client.scala.model.domain.operations.AbstractOperation
import amf.shapes.internal.domain.metamodel.`abstract`.{
  AbstractOperationModel,
  AbstractRequestModel,
  AbstractResponseModel
}

// TODO: unify with api model:
// Option 1: move part of Operation model to a generic common operation (with request, response, parameters) to core. ApiOperation on api-contract to inherits of that
// Option 2: move all model to amf-model new package. amf-model will contain all objects (including shapes? at least any shape will be needed) That module can be before amf-shapes or event before amf-aml solving the problem of json schema.

object QueryOperationModel extends AbstractOperationModel {

  override val key: Field = Name

  override val Request: Field = Field(
    QueryRequestModel,
    Core + "expects",
    ModelDoc(ModelVocabularies.Core, "expects", "Request information required by the operation"))

  override val Response: Field = Field(
    QueryResponseModel,
    Core + "returns",
    ModelDoc(ModelVocabularies.Core, "returns", "Response data returned by the operation"))

  override def modelInstance = QueryOperation()
}
