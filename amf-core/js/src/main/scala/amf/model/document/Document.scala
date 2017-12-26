package amf.model.document

import amf.core.model.document.{Document => CoreDocument}
import amf.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Document model class.
  */
@JSExportAll
case class Document(private[amf] val document: CoreDocument)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  @JSExportTopLevel("model.document.Document")
  def this() = this(CoreDocument())

  @JSExportTopLevel("model.document.Document")
  def this(domainElement: DomainElement) = this(CoreDocument().withEncodes(domainElement.element))

  override private[amf] val element = document

}