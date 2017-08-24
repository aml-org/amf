package amf.model

import scala.collection.JavaConverters._

/**
  * Request jvm class
  */
case class Request private[model] (private val request: amf.domain.Request) extends DomainElement {

  def this() = this(amf.domain.Request())

  val queryParameters: java.util.List[Parameter] = request.queryParameters.map(Parameter).asJava
  val headers: java.util.List[Parameter]         = request.headers.map(Parameter).asJava
  val payloads: java.util.List[Payload]          = request.payloads.map(Payload).asJava

  override def equals(other: Any): Boolean = other match {
    case that: Request =>
      (that canEqual this) &&
        request == that.request
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Request]

  override private[amf] def element: amf.domain.Request = request

  def withQueryParameters(parameters: java.util.List[Parameter]): this.type = {
    request.withQueryParameters(parameters.asScala.map(_.element))
    this
  }
  def withHeaders(headers: java.util.List[Parameter]): this.type = {
    request.withHeaders(headers.asScala.map(_.element))
    this
  }
  def withPayloads(payloads: java.util.List[Payload]): this.type = {
    request.withPayloads(payloads.asScala.map(_.element))
    this
  }

  def withQueryParameter(name: String): Parameter = Parameter(request.withQueryParameter(name))

  def withHeader(name: String): Parameter = Parameter(request.withHeader(name))

  def withPayload(): Payload = Payload(request.withPayload())
}
