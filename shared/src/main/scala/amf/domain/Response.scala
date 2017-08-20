package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.ResponseModel._

/**
  * Response internal model.
  */
case class Response(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String            = fields(Name)
  def description: String     = fields(Description)
  def statusCode: String      = fields(StatusCode)
  def headers: Seq[Parameter] = fields(Headers)
  def payloads: Seq[Payload]  = fields(Payloads)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withStatusCode(statusCode: String): this.type   = set(StatusCode, statusCode)
  def withHeaders(headers: Seq[Parameter]): this.type = setArray(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type = setArray(Payloads, payloads)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object Response {
  def apply(): Response = apply(Annotations())

  def apply(ast: AMFAST): Response = apply(Annotations(ast))

  def apply(annotations: Annotations): Response = new Response(Fields(), annotations)
}
