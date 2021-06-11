package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.core.client.platform.model.{BoolField, StrField}
import amf.plugins.domain.apicontract.models.{Parameter => InternalParameter}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}

/**
  * Parameter model class.
  */
@JSExportAll
case class Parameter(override private[amf] val _internal: InternalParameter)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Parameter")
  def this() = this(InternalParameter())

  def name: StrField                = _internal.name
  def parameterName: StrField       = _internal.parameterName
  def description: StrField         = _internal.description
  def required: BoolField           = _internal.required
  def deprecated: BoolField         = _internal.deprecated
  def allowEmptyValue: BoolField    = _internal.allowEmptyValue
  def style: StrField               = _internal.style
  def explode: BoolField            = _internal.explode
  def allowReserved: BoolField      = _internal.allowReserved
  def binding: StrField             = _internal.binding
  def schema: Shape                 = _internal.schema
  def payloads: ClientList[Payload] = _internal.payloads.asClient
  def examples: ClientList[Example] = _internal.examples.asClient

  /** Set name property of this Parameter. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set specific name of this Parameter. */
  def withParameterName(name: String): this.type = {
    _internal.withParameterName(name)
    this
  }

  /** Set description property of this Parameter. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set required property of this Parameter. */
  def withRequired(required: Boolean): this.type = {
    _internal.withRequired(required)
    this
  }

  /** Set deprecated property of this Parameter. */
  def withDeprecated(deprecated: Boolean): this.type = {
    _internal.withDeprecated(deprecated)
    this
  }

  /** Set allowEmptyValue property of this Parameter. */
  def withAllowEmptyValue(allowEmptyValue: Boolean): this.type = {
    _internal.withAllowEmptyValue(allowEmptyValue)
    this
  }

  /** Set style property of this Parameter. */
  def withStyle(style: String): this.type = {
    _internal.withStyle(style)
    this
  }

  /** Set explode property of this Parameter. */
  def withExplode(explode: Boolean): this.type = {
    _internal.withExplode(explode)
    this
  }

  /** Set allowReserved property of this Parameter. */
  def withAllowReserved(allowReserved: Boolean): this.type = {
    _internal.withAllowReserved(allowReserved)
    this
  }

  /** Set binding property of this Parameter. */
  def withBinding(binding: String): this.type = {
    _internal.withBinding(binding)
    this
  }

  /** Set payloads property of this Parameter. */
  def withPayloads(payloads: ClientList[Payload]): this.type = {
    _internal.withPayloads(payloads.asInternal)
    this
  }

  /** Set examples property of this Parameter. */
  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  /** Set schema property of this Parameter. */
  def withSchema(schema: Shape): this.type = {
    _internal.withSchema(schema)
    this
  }

  /** Set schema property of this Parameter. */
  def withObjectSchema(name: String): NodeShape = _internal.withObjectSchema(name)

  def withScalarSchema(name: String): ScalarShape = _internal.withScalarSchema(name)

  def withPayload(mediaType: String): Payload = _internal.withPayload(mediaType)

  def withExample(name: String): Example = _internal.withExample(Some(name))
}
