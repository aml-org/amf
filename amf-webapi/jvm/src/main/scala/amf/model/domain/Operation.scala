package amf.model.domain

import amf.plugins.domain.webapi.models

import scala.collection.JavaConverters._

/**
  * JS Operation model class.
  */
case class Operation private[model] (private val operation: models.Operation) extends DomainElement {

  def this() = this(models.Operation())

  def method: String                       = operation.method
  def name: String                         = operation.name
  def description: String                  = operation.description
  def deprecated: Boolean                  = operation.deprecated
  def summary: String                      = operation.summary
  def documentation: CreativeWork          = Option(operation.documentation).map(CreativeWork).orNull
  def schemes: java.util.List[String]         = Option(operation.schemes).getOrElse(Nil).asJava
  def accepts: java.util.List[String]         = Option(operation.accepts).getOrElse(Nil).asJava
  def contentType: java.util.List[String]     = Option(operation.contentType).getOrElse(Nil).asJava
  def request: Request                     = Option(operation.request).map(Request).orNull
  def responses: java.util.List[Response]     = Option(operation.responses).getOrElse(Nil).map(Response).asJava
  def security: java.util.List[DomainElement] = Option(operation.security).getOrElse(Nil).map(DomainElement(_)).asJava

  override private[amf] def element: models.Operation = operation

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
