package amf.model

import scala.collection.JavaConverters._

/**
  * JVM Request model class.
  */
case class Request private[model] (private val request: amf.domain.Request) extends DomainElement {

  def this() = this(amf.domain.Request())

  val queryParameters: java.util.List[Parameter] = request.queryParameters.map(Parameter).asJava
  val headers: java.util.List[Parameter]         = request.headers.map(Parameter).asJava
  val payloads: java.util.List[Payload]          = request.payloads.map(Payload).asJava
  val queryString: Shape                         = Option(request.queryString).map(Shape(_)).orNull

  override private[amf] def element: amf.domain.Request = request

  /** Set queryParameters property of this [[Request]]. */
  def withQueryParameters(parameters: java.util.List[Parameter]): this.type = {
    request.withQueryParameters(parameters.asScala.map(_.element))
    this
  }

  /** Set headers property of this [[Request]]. */
  def withHeaders(headers: java.util.List[Parameter]): this.type = {
    request.withHeaders(headers.asScala.map(_.element))
    this
  }

  /** Set payloads property of this [[Request]]. */
  def withPayloads(payloads: java.util.List[Payload]): this.type = {
    request.withPayloads(payloads.asScala.map(_.element))
    this
  }

  /**
    * Adds one [[Parameter]] to the queryParameters property of this [[Request]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withQueryParameter(name: String): Parameter = Parameter(request.withQueryParameter(name))

  /**
    * Adds one [[Parameter]] to the headers property of this [[Request]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = Parameter(request.withHeader(name))

  /** Adds one [[Payload]] to the payloads property of this [[Request]] and returns it for population. */
  def withPayload(): Payload = Payload(request.withPayload())

  /** Adds one [[Payload]] to the payloads property of this [[Request]] with the given media type and returns it for population. */
  def withPayload(mediaType: String): Payload = Payload(request.withPayload(Some(mediaType)))

  /** Set query string property of this [[Request]]. */
  def withQueryString(queryString: Shape): this.type = {
    request.withQueryString(queryString.shape)
    this
  }
}
