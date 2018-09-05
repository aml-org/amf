package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain.{DialectDomainElement, External}
import amf.plugins.document.vocabularies.model.document.{DialectInstancePatch => InternalPatchInstance}
import amf.plugins.document.vocabularies.model.domain.{DialectDomainElement => InternalDialectDomainElement}

import scala.scalajs.js.annotation.JSExportTopLevel

class DialectInstancePatch(private[amf] val _internal: InternalPatchInstance)
  extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  @JSExportTopLevel("model.document.DialectInstancePatch")
  def this() = this(InternalPatchInstance())

  def definedBy(): StrField                     = _internal.definedBy()
  def graphDependencies(): ClientList[StrField] = _internal.graphDependencies.asClient
  def externals: ClientList[External]           = _internal.externals.asClient

  override def encodes: DialectDomainElement =
    DialectDomainElement(_internal.encodes.asInstanceOf[InternalDialectDomainElement])

  def withDefinedBy(dialectId: String): DialectInstancePatch = {
    _internal.withDefinedBy(dialectId)
    this
  }

  def withGraphDependencies(ids: ClientList[String]): DialectInstancePatch = {
    _internal.withGraphDependencies(ids.asInternal)
    this
  }

  def withEncodes(encoded: DialectDomainElement): DialectInstancePatch = {
    _internal.withEncodes(encoded._internal)
    this
  }

  def withExternals(externals: ClientList[External]): DialectInstancePatch = {
    _internal.withExternals(externals.asInternal)
    this
  }
}
