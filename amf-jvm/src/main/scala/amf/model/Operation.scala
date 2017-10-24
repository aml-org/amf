package amf.model

import scala.collection.JavaConverters._

/**
  * JVM Operation model class.
  */
case class Operation private[model] (private val operation: amf.domain.Operation) extends DomainElement {

  def this() = this(amf.domain.Operation())

  val method: String                          = operation.method
  val name: String                            = operation.name
  val description: String                     = operation.description
  val deprecated: Boolean                     = operation.deprecated
  val summary: String                         = operation.summary
  val documentation: CreativeWork             = Option(operation.documentation).map(CreativeWork).orNull
  val schemes: java.util.List[String]         = operation.schemes.asJava
  val accepts: java.util.List[String]         = operation.accepts.asJava
  val contentType: java.util.List[String]     = operation.contentType.asJava
  val request: Request                        = Option(operation.request).map(Request).orNull
  val responses: java.util.List[Response]     = operation.responses.map(Response).asJava
  def security: java.util.List[DomainElement] = operation.security.map(DomainElement(_)).asJava

  override private[amf] def element: amf.domain.Operation = operation

  /** Set method property of this [[Operation]]. */
  def withMethod(method: String): this.type = {
    operation.withMethod(method)
    this
  }

  /** Set name property of this [[Operation]]. */
  def withName(name: String): this.type = {
    operation.withName(name)
    this
  }

  /** Set description property of this [[Operation]]. */
  def withDescription(description: String): this.type = {
    operation.withDescription(description)
    this
  }

  /** Set deprecated property of this [[Operation]]. */
  def withDeprecated(deprecated: Boolean): this.type = {
    operation.withDeprecated(deprecated)
    this
  }

  /** Set summary property of this [[Operation]]. */
  def withSummary(summary: String): this.type = {
    operation.withSummary(summary)
    this
  }

  /** Set documentation property of this [[Operation]] using a [[CreativeWork]]. */
  def withDocumentation(documentation: CreativeWork): this.type = {
    operation.withDocumentation(documentation.element)
    this
  }

  /** Set schemes property of this [[Operation]]. */
  def withSchemes(schemes: java.util.List[String]): this.type = {
    operation.withSchemes(schemes.asScala)
    this
  }

  /** Set accepts property of this [[Operation]]. */
  def withAccepts(accepts: java.util.List[String]): this.type = {
    operation.withAccepts(accepts.asScala)
    this
  }

  /** Set contentType property of this [[Operation]]. */
  def withContentType(contentType: java.util.List[String]): this.type = {
    operation.withContentType(contentType.asScala)
    this
  }

  /** Set request property of this [[Operation]]. */
  def withRequest(request: Request): this.type = {
    operation.withRequest(request.element)
    this
  }

  /** Set responses property of this [[Operation]]. */
  def withResponses(responses: java.util.List[Response]): this.type = {
    operation.withResponses(responses.asScala.map(_.element))
    this
  }

  /** Set security property of this [[Operation]]. */
  def withSecurity(security: java.util.List[DomainElement]): this.type = {
    operation.withSecurity(security.asScala.map(_.element))
    this
  }

  /**
    * Adds one [[Response]] to the responses property of this [[Operation]] and returns it for population.
    * Name property of the response is required.
    */
  def withResponse(name: String): Response = Response(operation.withResponse(name))
}
