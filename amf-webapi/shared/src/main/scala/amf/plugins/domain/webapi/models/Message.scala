package amf.plugins.domain.webapi.models

import amf.core.metamodel.Field
import amf.core.model.domain.{NamedDomainElement, Linkable}
import amf.core.model.{BoolField, StrField}
import amf.plugins.domain.shapes.models.{Example, CreativeWork}
import amf.plugins.domain.webapi.metamodel.MessageModel
import amf.plugins.domain.webapi.metamodel.MessageModel._
import amf.plugins.domain.webapi.models.bindings.MessageBinding

trait Message extends NamedDomainElement with Linkable {

  def description: StrField         = fields.field(Description)
  def isAbstract: BoolField         = fields.field(IsAbstract)
  def documentation: CreativeWork   = fields.field(Documentation)
  def tags: Seq[Tag]                = fields.field(Tags)
  def examples: Seq[Example]        = fields.field(Examples)
  def payloads: Seq[Payload]        = fields.field(Payloads)
  def correlationId: CorrelationId  = fields.field(MessageModel.CorrelationId)
  def displayName: StrField         = fields.field(DisplayName)
  def title: StrField               = fields.field(Title)
  def summary: StrField             = fields.field(Summary)
  def bindings: Seq[MessageBinding] = fields.field(Bindings)
  def headers: Seq[Parameter]       = fields.field(Headers)

  def withDescription(description: String): this.type            = set(Description, description)
  def isAbstract(isAbstract: Boolean): this.type                 = set(IsAbstract, isAbstract)
  def withDocumentation(documentation: CreativeWork): this.type  = set(Documentation, documentation)
  def withTags(tags: Seq[Tag]): this.type                        = setArray(Tags, tags)
  def withExamples(examples: Seq[Example]): this.type            = setArray(Examples, examples)
  def withPayloads(payloads: Seq[Payload]): this.type            = setArray(Payloads, payloads)
  def withCorrelationId(correlationId: CorrelationId): this.type = set(MessageModel.CorrelationId, correlationId)
  def withDisplayName(displayName: String): this.type            = set(DisplayName, displayName)
  def withTitle(title: String): this.type                        = set(Title, title)
  def withSummary(summary: String): this.type                    = set(Summary, summary)
  def withBindings(bindings: Seq[MessageBinding]): this.type     = setArray(Bindings, bindings)
  def withHeaders(headers: Seq[Parameter]): this.type            = setArray(Headers, headers)

  def withPayload(mediaType: Option[String] = None): Payload = {
    val result = Payload()
    mediaType.map(result.withMediaType)
    add(Payloads, result)
    result
  }

  override protected def nameField: Field = MessageModel.Name
}
