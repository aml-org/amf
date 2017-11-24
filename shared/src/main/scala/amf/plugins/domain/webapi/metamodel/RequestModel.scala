package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Array
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.metamodel.ShapeModel
import amf.plugins.domain.webapi.models.Request
import amf.framework.vocabulary.Namespace.Http
import amf.framework.vocabulary.ValueType

/**
  * Request metamodel.
  */
object RequestModel extends DomainElementModel {

  val QueryParameters = Field(Array(ParameterModel), Http + "parameter")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val Payloads = Field(Array(PayloadModel), Http + "payload")

  val QueryString = Field(ShapeModel, Http + "queryString")

  override val `type`: List[ValueType] = Http + "Request" :: DomainElementModel.`type`

  override def fields: List[Field] = List(QueryParameters, Headers, Payloads, QueryString) ++ DomainElementModel.fields

  override def modelInstance = Request()
}
