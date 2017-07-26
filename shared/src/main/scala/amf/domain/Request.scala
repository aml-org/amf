package amf.domain

import amf.builder.RequestBuilder
import amf.metadata.domain.RequestModel._

/**
  * Request internal model.
  */
case class Request(fields: Fields) extends DomainElement {
  override type T = Request

  val queryParameters: Seq[Parameter] = fields(QueryParameters)
  val headers: Seq[Parameter]         = fields(Headers)
  val payloads: Seq[Payload]          = fields(Payloads)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Request]

  override def equals(other: Any): Boolean = other match {
    case that: Request =>
      (that canEqual this) &&
        queryParameters == that.queryParameters &&
        headers == that.headers &&
        payloads == that.payloads

    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(queryParameters, headers, payloads)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Request($queryParameters, $headers, $payloads)"

  override def toBuilder: RequestBuilder = RequestBuilder(fields)
}

object Request {
  def apply(fields: Fields): Request = new Request(fields)
}
