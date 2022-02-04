package amf.apiinstance.internal.metamodel.domain

import amf.core.internal.metamodel.Type._
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiInstance
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.apiinstance.client.scala.model.domain.Proxy
import amf.core.client.scala.vocabulary.ValueType

object ProxyModel extends DomainElementModel with NameFieldSchema with DescriptionField {
  override def modelInstance: AmfObject = Proxy()

  val ProtocolListeners =
    Field(Array(ProtocolListenerModel),
      ApiInstance + "listeners",
      ModelDoc(ModelVocabularies.ApiInstance, "listeners", "Listeners for the different L4 protocols supported by this proxy")
    )


  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiInstance,
    "Proxy",
    "API instance acting as a proxy for an API"
  )

  override val `type`: List[ValueType] = ApiInstance + "Proxy" :: DomainElementModel.`type`

  override def fields: List[Field] = List(
    ProtocolListeners
  )
}
