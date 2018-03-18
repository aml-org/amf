package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.convert.CoreBaseConverter
import amf.client.model.StrField
import amf.client.model.domain.{DialectDomainElement, DomainElement}
import amf.plugins.document.vocabularies.model.document.{DialectInstance => InternalDialectInstance, DialectInstanceFragment => InternalDialectInstanceFragment, DialectInstanceLibrary => InternalDialectInstanceLibrary}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class DialectInstance(private[amf] val _internal: InternalDialectInstance) extends BaseUnit with EncodesModel with DeclaresModel {

  @JSExportTopLevel("model.document.DialectInstance")
  def this() = this(InternalDialectInstance())

  def definedBy(): StrField = _internal.definedBy()
  def withDefinedBy(dialectId: String) = {
    _internal.withDefinedBy(dialectId)
    this
  }
  def withGraphDependencies(ids: ClientList[String]) = {
    _internal.withGraphDependencies(ids.asInternal)
    this
  }

  override def encodes: DialectDomainElement = DialectDomainElement(_internal.encodes.asInstanceOf[DialectDomainElement])
  def withEncodes(encoded: DialectDomainElement) = {
    _internal.withEncodes(encoded._internal)
    this
  }
}


@JSExportAll
class DialectInstanceFragment(private[amf] val _internal: InternalDialectInstanceFragment) extends BaseUnit with EncodesModel {

  @JSExportTopLevel("model.document.DialectInstanceFragment")
  def this() = this(InternalDialectInstanceFragment())

  override def encodes: DialectDomainElement = DialectDomainElement(_internal.encodes.asInstanceOf[DialectDomainElement])
  def withEncodes(encoded: DialectDomainElement) = {
    _internal.withEncodes(encoded._internal)
    this
  }
}

@JSExportAll
class DialectInstanceLibrary(private[amf] val _internal: InternalDialectInstanceLibrary) extends BaseUnit with DeclaresModel {

  @JSExportTopLevel("model.document.DialectInstanceLibrary")
  def this() = this(InternalDialectInstanceLibrary())

}
