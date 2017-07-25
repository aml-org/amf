package amf.domain

import amf.builder.ResponseBuilder
import amf.metadata.domain.ResponseModel._

/**
  * Response internal model.
  */
class Response(override val fields: Fields) extends FieldHolder(fields) with DomainElement {
  override type T = Response

  val name: String            = fields get Name
  val description: String     = fields get Description
  val statusCode: String      = fields get StatusCode
  val headers: Seq[Parameter] = fields get Headers
  val payloads: Seq[Payload]  = fields get Payloads

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
