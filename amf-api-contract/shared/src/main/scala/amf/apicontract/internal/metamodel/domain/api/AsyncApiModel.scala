package amf.apicontract.internal.metamodel.domain.api
import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object AsyncApiModel extends ApiModel {

  override val `type`: List[ValueType] = ApiContract + "AsyncAPI" :: BaseApiModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "AsyncAPI",
    "Top level element describing a asynchronous API"
  )

  override def modelInstance: AmfObject = AsyncApi()
}
