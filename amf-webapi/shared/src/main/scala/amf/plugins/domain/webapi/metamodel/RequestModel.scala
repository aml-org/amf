package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.{Http, Schema}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Request

/**
  * Request metaModel.
  */
object RequestModel extends DomainElementModel with DescriptionField {

  val Required = Field(Bool, Http + "required")

  val QueryParameters = Field(Array(ParameterModel), Http + "parameter")

  val Headers = Field(Array(ParameterModel), Http + "header")

  val Payloads = Field(Array(PayloadModel), Http + "payload")

  val QueryString = Field(ShapeModel, Http + "queryString")

  val UriParameters = Field(Array(ParameterModel), Http + "uriParameter")

  val CookieParameters = Field(Array(ParameterModel), Http + "cookieParameter")

  override val `type`: List[ValueType] = Http + "Request" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Description, Required, QueryParameters, Headers, Payloads, QueryString, UriParameters, CookieParameters) ++ DomainElementModel.fields

  override def modelInstance = Request()
}
