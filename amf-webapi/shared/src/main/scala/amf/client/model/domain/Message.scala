package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{BoolField, StrField}
import amf.plugins.domain.webapi.models.{Message => InternalMessage}

import scala.scalajs.js.annotation.JSExportAll

/**
  * Message model class.
  */
@JSExportAll
trait Message extends NamedDomainElement with DomainElement with Linkable {

  override private[amf] val _internal: InternalMessage

  override def name: StrField       = _internal.name
  def description: StrField         = _internal.description
  def isAbstract: BoolField         = _internal.isAbstract
  def documentation: CreativeWork   = _internal.documentation
  def tags: ClientList[Tag]         = _internal.tags.asClient
  def examples: ClientList[Example] = _internal.examples.asClient
  def payloads: ClientList[Payload] = _internal.payloads.asClient
  def correlationId: CorrelationId  = _internal.correlationId
  def displayName: StrField         = _internal.displayName
  def title: StrField               = _internal.title
  def summary: StrField             = _internal.summary
  //  def bindings: Seq[_] = fields.field(Bindings) // TODO replace message bindings

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

  //  def withBindings(bindings: _): this.type = setArray(Bindings, bindings)

  def withPayload(mediaType: ClientOption[String]): Payload = _internal.withPayload(mediaType.toScala)

  def withPayload(): Payload = _internal.withPayload()

}
