package amf.model

import scala.collection.JavaConverters._

/**
  * response jvm class
  */
case class Response private[model] (private val response: amf.domain.Response) extends DomainElement {

  def this() = this(amf.domain.Response())

  val name: String                       = response.name
  val description: String                = response.description
  val statusCode: String                 = response.statusCode
  val headers: java.util.List[Parameter] = response.headers.map(Parameter).asJava
  val payloads: java.util.List[Payload]  = response.payloads.map(Payload).asJava

  override def equals(other: Any): Boolean = other match {
    case that: Response =>
      (that canEqual this) &&
        response == that.response
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Response]

  override private[amf] def element: amf.domain.Response = response

  def withName(name: String): this.type = {
    response.withName(name)
    this
  }
  def withDescription(description: String): this.type = {
    response.withDescription(description)
    this
  }
  def withStatusCode(statusCode: String): this.type = {
    response.withStatusCode(statusCode)
    this
  }
  def withHeaders(headers: java.util.List[Parameter]): this.type = {
    response.withHeaders(headers.asScala.map(_.element))
    this
  }
  def withPayloads(payloads: java.util.List[Payload]): this.type = {
    response.withPayloads(payloads.asScala.map(_.element))
    this
  }

  def withHeader(name: String): Parameter = Parameter(response.withHeader(name))

  def withPayload(): Payload = Payload(response.withPayload())

}
