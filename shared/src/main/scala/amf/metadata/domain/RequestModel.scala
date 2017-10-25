package amf.metadata.domain
import amf.metadata.Field
import amf.metadata.Type.Array
import amf.metadata.shape.ShapeModel
import amf.vocabulary.Namespace.Http
import amf.vocabulary.ValueType

/**
  * Request metamodel.
  */
object RequestModel extends DomainElementModel {

  val QueryParameters = Field(Array(ParameterModel), Http + "parameter")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val Payloads = Field(Array(PayloadModel), Http + "payload")

  val QueryString = Field(ShapeModel, Http + "queryString")

  override val `type`: List[ValueType] = Http + "Request" :: DomainElementModel.`type`

  override def fields: List[Field] = List(QueryParameters, Headers, Payloads) ++ DomainElementModel.fields
}
