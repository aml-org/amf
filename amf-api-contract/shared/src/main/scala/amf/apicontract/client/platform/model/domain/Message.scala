package amf.apicontract.client.platform.model.domain

import amf.apicontract.client.platform.model.domain.bindings.MessageBindings
import amf.apicontract.client.scala.model.domain
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.platform.model.{BoolField, StrField}
import amf.apicontract.client.scala.model.domain.{Message => InternalMessage}
import amf.shapes.client.platform.model.domain.{CreativeWork, Example, NodeShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Message model class.
  */
@JSExportAll
class Message(override private[amf] val _internal: InternalMessage)
    extends NamedDomainElement
    with DomainElement
    with Linkable {

  @JSExportTopLevel("model.domain.Message")
  def this() = this(InternalMessage())

  override def name: StrField             = _internal.name
  def description: StrField               = _internal.description
  def isAbstract: BoolField               = _internal.isAbstract
  def documentation: CreativeWork         = _internal.documentation
  def tags: ClientList[Tag]               = _internal.tags.asClient
  def examples: ClientList[Example]       = _internal.examples.asClient
  def headerExamples: ClientList[Example] = _internal.headerExamples.asClient
  def payloads: ClientList[Payload]       = _internal.payloads.asClient
  def correlationId: CorrelationId        = _internal.correlationId
  def displayName: StrField               = _internal.displayName
  def title: StrField                     = _internal.title
  def summary: StrField                   = _internal.summary
  def bindings: MessageBindings           = _internal.bindings
  def headerSchema: NodeShape             = _internal.headerSchema

  /** Set name property of this Response. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }
  def withAbstract(isAbstract: Boolean): this.type = {
    _internal.isAbstract
    this
  }
  def withDocumentation(documentation: CreativeWork): this.type = {
    _internal.withDocumentation(documentation)
    this
  }
  def withTags(tags: ClientList[Tag]): this.type = {
    _internal.withTags(tags.asInternal)
    this
  }
  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }
  def withHeaderExamples(examples: ClientList[Example]): this.type = {
    _internal.withHeaderExamples(examples.asInternal)
    this
  }
  def withPayloads(payloads: ClientList[Payload]): this.type = {
    _internal.withPayloads(payloads.asInternal)
    this
  }
  def withCorrelationId(correlationId: CorrelationId): this.type = {
    _internal.withCorrelationId(correlationId)
    this
  }
  def withDisplayName(displayName: String): this.type = {
    _internal.withDisplayName(displayName)
    this
  }
  def withTitle(title: String): this.type = {
    _internal.withTitle(title)
    this
  }
  def withSummary(summary: String): this.type = {
    _internal.withSummary(summary)
    this
  }

  def withBindings(bindings: MessageBindings): this.type = {
    _internal.withBindings(bindings)
    this
  }

  def withHeaderSchema(schema: NodeShape): this.type = {
    _internal.withHeaderSchema(schema)
    this
  }

  def withPayload(mediaType: ClientOption[String]): Payload = _internal.withPayload(mediaType.toScala)

  def withPayload(mediaType: String): Payload = _internal.withPayload(Some(mediaType))

  def withPayload(): Payload = _internal.withPayload()

  override def linkCopy(): Message = _internal.linkCopy()
}
