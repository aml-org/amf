package amf.plugins.domain.apicontract.metamodel.api

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.plugins.domain.apicontract.models.api.WebApi

object WebApiModel extends ApiModel {

  override val `type`: List[ValueType] = ApiContract + "WebAPI" :: BaseApiModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "WebAPI",
    "Top level element describing a HTTP API"
  )

  override def modelInstance: AmfObject = WebApi()
}
