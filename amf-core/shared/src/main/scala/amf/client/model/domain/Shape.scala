package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.StrField
import amf.core.model.domain.{Shape => InternalShape}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait Shape extends DomainElement with Linkable with NamedDomainElement {

  override private[amf] val _internal: InternalShape

  def name: StrField            = _internal.name
  def displayName: StrField     = _internal.displayName
  def description: StrField     = _internal.description
  def defaultValue: DataNode    = _internal.default
  def defaultValueStr: StrField = _internal.defaultString

  def values: ClientList[DataNode] = _internal.values.asClient
  def inherits: ClientList[Shape]  = _internal.inherits.asClient
  def or: ClientList[Shape]        = _internal.or.asClient
  def and: ClientList[Shape]       = _internal.and.asClient
  def xone: ClientList[Shape]      = _internal.xone.asClient
  def not: Shape                   = _internal.not

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

  def withValues(values: ClientList[DataNode]): this.type = {
    _internal.withValues(values.asInternal)
    this
  }

  def withInherits(inherits: ClientList[Shape]): this.type = {
    _internal.withInherits(inherits.asInternal)
    this
  }

  def withOr(subShapes: ClientList[Shape]): this.type = {
    _internal.withOr(subShapes.asInternal)
    this
  }

  def withAnd(subShapes: ClientList[Shape]): this.type = {
    _internal.withAnd(subShapes.asInternal)
    this
  }

  def withXone(subShapes: ClientList[Shape]): this.type = {
    _internal.withXone(subShapes.asInternal)
    this
  }

  def withNode(shape: Shape): this.type = {
    _internal.withNot(shape)
    this
  }

  def withDefaultStr(value: String): this.type = {
    _internal.withDefaultStr(value)
    this
  }
}
