package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.ResponseModel._

/**
  * Response internal model.
  */
case class Response(fields: Fields, annotations: Annotations) extends DomainElement {

  val name: String            = fields(Name)
  val description: String     = fields(Description)
  val statusCode: String      = fields(StatusCode)
  val headers: Seq[Parameter] = fields(Headers)
  val payloads: Seq[Payload]  = fields(Payloads)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withStatusCode(statusCode: String): this.type   = set(StatusCode, statusCode)
  def withHeaders(headers: Seq[Parameter]): this.type = set(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type = set(Payloads, payloads)
}

object Response {
  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): Response =
    new Response(fields, annotations)

  def apply(ast: AMFAST): Response = new Response(Fields(), Annotations(ast))
}
