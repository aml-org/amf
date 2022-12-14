package amf.apicontract.client.platform.model.domain

import amf.apicontract.client.platform.model.domain.federation.{EndPointFederationMetadata, ParameterFederationMetadata}
import amf.apicontract.client.scala.model.domain.{Parameter => InternalParameter}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.{BoolField, StrField}
import amf.shapes.client.platform.model.domain.Example
import amf.shapes.client.platform.model.domain.operations.AbstractParameter

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Parameter model class.
  */
@JSExportAll
case class Parameter(override private[amf] val _internal: InternalParameter) extends AbstractParameter(_internal) {

  @JSExportTopLevel("Parameter")
  def this() = this(InternalParameter())

  def deprecated: BoolField         = _internal.deprecated
  def allowEmptyValue: BoolField    = _internal.allowEmptyValue
  def style: StrField               = _internal.style
  def explode: BoolField            = _internal.explode
  def allowReserved: BoolField      = _internal.allowReserved
  def payloads: ClientList[Payload] = _internal.payloads.asClient
  def examples: ClientList[Example] = _internal.examples.asClient

  def federationMetadata: ParameterFederationMetadata = _internal.federationMetadata

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

  def withPayload(mediaType: String): Payload = _internal.withPayload(mediaType)

  def withExample(name: String): Example = _internal.withExample(Some(name))
}
