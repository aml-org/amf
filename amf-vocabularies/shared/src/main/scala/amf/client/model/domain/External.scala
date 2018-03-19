package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{External => InternalExternal}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class External(override private[amf] val _internal: InternalExternal) extends DomainElement {

  @JSExportTopLevel("model.domain.External")
  def this() = this(InternalExternal())

  def withAlias(alias: String) = {
    _internal.withAlias(alias)
    this
  }

  def withBase(base: String) = {
    _internal.withBase(base)
    this
  }

  def alias: StrField = _internal.alias
  def base: StrField = _internal.base
}
