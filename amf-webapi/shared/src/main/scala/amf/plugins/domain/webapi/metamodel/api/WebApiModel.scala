package amf.plugins.domain.webapi.metamodel.api

import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.api.WebApi

object WebApiModel extends ApiModel {

  override val `type`: List[ValueType] = ApiContract + "WebAPI" :: super.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "WebAPI",
    "Top level element describing a HTTP API"
  )

  override def modelInstance: AmfObject = WebApi()
}
