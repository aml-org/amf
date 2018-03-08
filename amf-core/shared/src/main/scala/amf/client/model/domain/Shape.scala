package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.StrField
import amf.core.model.domain.{NamedDomainElement, Shape => InternalShape}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait Shape extends DomainElement with Linkable with NamedDomainElement {

  override private[amf] val _internal: InternalShape

  def name: StrField               = _internal.name
  def displayName: StrField        = _internal.displayName
  def description: StrField        = _internal.description
  def defaultValue: DataNode       = _internal.default
  def values: ClientList[StrField] = _internal.values.asClient
  def inherits: ClientList[Shape]  = _internal.inherits.asClient

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withDisplayName(name: String): this.type = {
    _internal.withDisplayName(name)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withDefaultValue(default: DataNode): this.type = {
    _internal.withDefault(default)
    this
  }

  def withValues(values: ClientList[String]): this.type = {
    _internal.withValues(values.asInternal)
    this
  }

  def withInherits(inherits: ClientList[Shape]): this.type = {
    _internal.withInherits(inherits.asInternal)
    this
  }
}
