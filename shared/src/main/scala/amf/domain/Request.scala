package amf.domain

import amf.framework.parser.Annotations
import amf.metadata.domain.RequestModel._
import amf.shape.Shape

/**
  * Request internal model.
  */
case class Request(fields: Fields, annotations: Annotations) extends DomainElement {

  def queryParameters: Seq[Parameter] = fields(QueryParameters)
  def headers: Seq[Parameter]         = fields(Headers)
  def payloads: Seq[Payload]          = fields(Payloads)
  def queryString: Shape              = fields(QueryString)

  def withQueryParameters(parameters: Seq[Parameter]): this.type = setArray(QueryParameters, parameters)
  def withHeaders(headers: Seq[Parameter]): this.type            = setArray(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type            = setArray(Payloads, payloads)
  def withQueryString(queryString: Shape): this.type             = set(QueryString, queryString)

  override def adopted(parent: String): this.type = withId(parent + "/request")

  def withQueryParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(QueryParameters, result)
    result
  }

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  def withPayload(mediaType: Option[String] = None): Payload = {
    val result = Payload()
    mediaType.map(result.withMediaType)
    add(Payloads, result)
    result
  }

}

object Request {

  def apply(): Request = apply(Annotations())

  def apply(annotations: Annotations): Request = new Request(Fields(), annotations)
}
