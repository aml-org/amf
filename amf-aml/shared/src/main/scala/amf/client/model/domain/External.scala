package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{External => InternalExternal}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class External(override private[amf] val _internal: InternalExternal) extends DomainElement {

  @JSExportTopLevel("model.domain.External")
  def this() = this(InternalExternal())

  def alias: StrField = _internal.alias
  def base: StrField  = _internal.base

  def withAlias(alias: String): External = _internal.withAlias(alias)
  def withBase(base: String): External   = _internal.withBase(base)
}
