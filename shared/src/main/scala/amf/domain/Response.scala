package amf.domain

import amf.builder.ResponseBuilder
import amf.metadata.domain.ResponseModel._

/**
  * Response internal model.
  */
case class Response(fields: Fields) extends DomainElement {
  override type T = Response

  val name: String            = fields(Name)
  val description: String     = fields(Description)
  val statusCode: String      = fields(StatusCode)
  val headers: Seq[Parameter] = fields(Headers)
  val payloads: Seq[Payload]  = fields(Payloads)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Response]

  override def equals(other: Any): Boolean = other match {
    case that: Response =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        statusCode == that.statusCode &&
        headers == that.headers &&
        payloads == that.payloads

    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, description, statusCode, headers, payloads)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Response($name, $description, $statusCode, $headers, $payloads)"

  override def toBuilder: ResponseBuilder = ResponseBuilder(fields)
}

object Response {
  def apply(fields: Fields): Response = new Response(fields)
}
