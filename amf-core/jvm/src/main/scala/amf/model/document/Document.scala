package amf.model.document

import amf.core.model.document.{Document => CoreDocument}
import amf.model.domain.DomainElement

/**
  * JS Document model class.
  */
case class Document(private[amf] val document: CoreDocument)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  def this() = this(CoreDocument())

  def this(domainElement: DomainElement) = this(CoreDocument().withEncodes(domainElement.element))

  override private[amf] val element = document

}