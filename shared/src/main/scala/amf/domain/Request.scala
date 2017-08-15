package amf.domain

import amf.metadata.domain.RequestModel._

/**
  * Request internal model.
  */
case class Request(fields: Fields = Fields(), annotations: Annotations = new Annotations()) extends DomainElement {

  val queryParameters: Seq[Parameter] = fields(QueryParameters)
  val headers: Seq[Parameter]         = fields(Headers)
  val payloads: Seq[Payload]          = fields(Payloads)

  def withQueryParameters(parameters: Seq[Parameter]): this.type = set(QueryParameters, parameters)
  def withHeaders(headers: Seq[Parameter]): this.type            = set(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type            = set(Payloads, payloads)
}
