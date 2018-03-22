package amf.client.model.document

import amf.client.model.domain.DialectDomainElement
import amf.plugins.document.vocabularies.model.document.{DialectInstanceFragment => InternalDialectInstanceFragment}
import amf.plugins.document.vocabularies.model.domain.{DialectDomainElement => InternalDialectDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class DialectInstanceFragment(private[amf] val _internal: InternalDialectInstanceFragment)
    extends BaseUnit
    with EncodesModel {

  @JSExportTopLevel("model.document.DialectInstanceFragment")
  def this() = this(InternalDialectInstanceFragment())

  override def encodes: DialectDomainElement =
    DialectDomainElement(_internal.encodes.asInstanceOf[InternalDialectDomainElement])

  def withEncodes(encoded: DialectDomainElement): DialectInstanceFragment = {
    _internal.withEncodes(encoded._internal)
    this
  }
}
