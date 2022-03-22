package amf.graphql.internal.spec.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Query, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.graphql.client.scala.model.domain.QueryRequest
import amf.shapes.client.scala.model.domain.operations.AbstractRequest
import amf.shapes.internal.domain.metamodel.`abstract`.{AbstractOperationModel, AbstractRequestModel}

object QueryRequestModel extends AbstractRequestModel {

  override val QueryParameters = Field(
    Array(QueryParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.Query, "parameter", "Parameters associated to the communication model")
  )

  override val `type`: List[ValueType] = List(Query + "Request") ++ AbstractRequestModel.`type`

  override def modelInstance = QueryRequest()

}
