package amf.model

import amf.plugins.domain.webapi.models

import scala.collection.JavaConverters._

/**
  * JVM Response model class.
  */
case class Response private[model] (private val response: models.Response) extends DomainElement {

  def this() = this(models.Response())

  val name: String                                       = response.name
  val description: String                                = response.description
  val statusCode: String                                 = response.statusCode
  val headers: java.util.List[Parameter]                 = response.headers.map(Parameter).asJava
  val payloads: java.util.List[Payload]                  = response.payloads.map(Payload).asJava
  val examples: java.util.List[Example]                  = response.examples.map(Example).asJava
  override private[amf] def element: models.Response = response

  /** Set name property of this [[Response]]. */
  def withName(name: String): this.type = {
    response.withName(name)
    this
  }

  /** Set description property of this [[Response]]. */
  def withDescription(description: String): this.type = {
    response.withDescription(description)
    this
  }

  /** Set statusCode property of this [[Response]]. */
  def withStatusCode(statusCode: String): this.type = {
    response.withStatusCode(statusCode)
    this
  }

  /** Set headers property of this [[Response]]. */
  def withHeaders(headers: java.util.List[Parameter]): this.type = {
    response.withHeaders(headers.asScala.map(_.element))
    this
  }

  /** Set payloads property of this [[Response]]. */
  def withPayloads(payloads: java.util.List[Payload]): this.type = {
    response.withPayloads(payloads.asScala.map(_.element))
    this
  }

  /** Set examples property of this [[Response]]. */
  def withExamples(examples: java.util.List[Example]): this.type = {
    response.withExamples(examples.asScala.map(_.element))
    this
  }

  /**
    * Adds one [[Parameter]] to the headers property of this [[Response]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = Parameter(response.withHeader(name))

  /** Adds one [[Payload]] to the payloads property of this [[Response]] and returns it for population. */
  def withPayload(): Payload = Payload(response.withPayload())

  /** Adds one [[Payload]] to the payloads property of this [[Response]] with the given media type and returns it for population. */
  def withPayload(mediaType: String): Payload = Payload(response.withPayload(Some(mediaType)))
}
