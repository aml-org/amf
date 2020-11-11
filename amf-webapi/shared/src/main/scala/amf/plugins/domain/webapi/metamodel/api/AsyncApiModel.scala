package amf.plugins.domain.webapi.metamodel.api
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.api.AsyncApi

object AsyncApiModel extends ApiModel {

  override val `type`: List[ValueType] = ApiContract + "AsyncAPI" :: BaseApiModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "API",
    "Top level element describing a asynchronous API"
  )

  override def modelInstance: AmfObject = AsyncApi()
}
