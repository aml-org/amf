package amf.apiinstance.internal.metamodel.domain

import amf.apiinstance.client.scala.model.domain.UpstreamService
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiInstance
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.Type.{Array, Bool, Int, Str}

object UpstreamServiceModel extends DomainElementModel with NameFieldSchema with DescriptionField {

  override def modelInstance: AmfObject = UpstreamService()

  val Host: Field = Field(Array(Str),
    ApiInstance + "host",
    ModelDoc(ModelVocabularies.ApiInstance, "host", "host targeting the network request"))

  val Route: Field = Field(Array(RouteModel),
    ApiInstance + "route",
    ModelDoc(ModelVocabularies.ApiInstance, "route", "HTTP route from downstream service to upstream service based on some routing conditions"))


  override val `type`: List[ValueType] = ApiInstance + "UpstreamService" :: DomainElementModel.`type`

  override def fields: List[Field] = List(
    Name,
    Description,
    Host,
    Route
  )  ++ DomainElementModel.fields
}
