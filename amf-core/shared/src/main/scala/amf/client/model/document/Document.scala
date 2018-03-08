package amf.client.model.document

import amf.client.convert.CoreClientConverters._
import amf.client.model.domain.DomainElement
import amf.core.model.document.{Document => InternalDocument}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Document model class.
  */
@JSExportAll
class Document(private[amf] val _internal: InternalDocument) extends BaseUnit with EncodesModel with DeclaresModel {

  @JSExportTopLevel("model.document.Document")
  def this() = this(InternalDocument())

  @JSExportTopLevel("model.document.Document")
  def this(encoding: DomainElement) = this(InternalDocument().withEncodes(encoding))
}
