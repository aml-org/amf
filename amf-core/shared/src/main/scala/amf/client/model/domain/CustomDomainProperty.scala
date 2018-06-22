package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.StrField
import amf.core.model.domain.extensions.{CustomDomainProperty => InternalCustomDomainProperty}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.domain.CustomDomainProperty")
@JSExportAll
case class CustomDomainProperty(private[amf] val _internal: InternalCustomDomainProperty)
    extends DomainElement
    with Linkable {

  @JSExportTopLevel("model.domain.CustomDomainProperty")
  def this() = this(InternalCustomDomainProperty())

  def name: StrField = _internal.name

  def displayName: StrField = _internal.displayName

  def description: StrField = _internal.description

  def domain: ClientList[StrField] = _internal.domain.asClient

  def schema: Shape = _internal.schema

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withDisplayName(displayName: String): this.type = {
    _internal.withDisplayName(displayName)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withDomain(domain: ClientList[String]): this.type = {
    _internal.withDomain(domain.asInternal)
    this
  }

  def withSchema(schema: Shape): this.type = {
    _internal.withSchema(schema)
    this
  }

  override def linkCopy(): CustomDomainProperty = _internal.linkCopy()
}
