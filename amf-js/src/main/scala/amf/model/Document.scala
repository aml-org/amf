package amf.model

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Document model class.
  */
@JSExportAll
case class Document(private[amf] val document: amf.document.BaseUnit)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  @JSExportTopLevel("Document")
  def this() = this(amf.document.Document())

  @JSExportTopLevel("Document")
  def this(webApi: WebApi) = this(amf.document.Document().withEncodes(webApi.element))

  override private[amf] val element = document
}
