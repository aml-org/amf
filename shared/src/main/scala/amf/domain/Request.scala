package amf.domain

import amf.metadata.domain.RequestModel._

/**
  * Request internal model.
  */
case class Request(fields: Fields = Fields(), annotations: Annotations = new Annotations()) extends DomainElement {

  def queryParameters: Seq[Parameter] = fields(QueryParameters)
  def headers: Seq[Parameter]         = fields(Headers)
  def payloads: Seq[Payload]          = fields(Payloads)

  def withQueryParameters(parameters: Seq[Parameter]): this.type = setArray(QueryParameters, parameters)
  def withHeaders(headers: Seq[Parameter]): this.type            = setArray(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type            = setArray(Payloads, payloads)
}
