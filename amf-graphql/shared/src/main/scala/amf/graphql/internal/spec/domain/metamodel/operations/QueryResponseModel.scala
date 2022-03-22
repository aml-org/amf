package amf.graphql.internal.spec.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Query, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.graphql.client.scala.model.domain.QueryResponse
import amf.shapes.client.scala.model.domain.operations.AbstractResponse
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractResponseModel

object QueryResponseModel extends AbstractResponseModel {

  override val Payload: Field = Field(
    QueryPayloadModel,
    ApiContract + "payload",
    ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  override val `type`: List[ValueType] = Query + "Response" :: AbstractResponseModel.`type`

  override def modelInstance = QueryResponse()

}
