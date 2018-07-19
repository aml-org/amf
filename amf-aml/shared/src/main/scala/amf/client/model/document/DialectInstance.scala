package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain.{DialectDomainElement, External}
import amf.plugins.document.vocabularies.model.document.{DialectInstance => InternalDialectInstance}
import amf.plugins.document.vocabularies.model.domain.{DialectDomainElement => InternalDialectDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class DialectInstance(private[amf] val _internal: InternalDialectInstance)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  @JSExportTopLevel("model.document.DialectInstance")
  def this() = this(InternalDialectInstance())

  def definedBy(): StrField                     = _internal.definedBy()
  def graphDependencies(): ClientList[StrField] = _internal.graphDependencies.asClient
  def externals: ClientList[External]           = _internal.externals.asClient

  override def encodes: DialectDomainElement =
    DialectDomainElement(_internal.encodes.asInstanceOf[InternalDialectDomainElement])

  def withDefinedBy(dialectId: String): DialectInstance = {
    _internal.withDefinedBy(dialectId)
    this
  }

  def withGraphDependencies(ids: ClientList[String]): DialectInstance = {
    _internal.withGraphDependencies(ids.asInternal)
    this
  }

  def withEncodes(encoded: DialectDomainElement): DialectInstance = {
    _internal.withEncodes(encoded._internal)
    this
  }

  def withExternals(externals: ClientList[External]): DialectInstance = {
    _internal.withExternals(externals.asInternal)
    this
  }
}
