package amf.model

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}

/**
  * JS Document model class.
  */
@JSExportAll
case class Document(private[amf] val document: amf.framework.document.Document)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  @JSExportTopLevel("Document")
  def this() = this(amf.framework.document.Document())

  @JSExportTopLevel("Document")
  def this(webApi: WebApi) = this(amf.framework.document.Document().withEncodes(webApi.element))

  @JSExport
  def resolve(profile: String): Document = Document(document.resolve(profile))

  override private[amf] val element = document
}

@JSExportAll
class Overlay(private[amf] val overlay: amf.framework.document.Overlay) extends Document(overlay) {

  def this() = this(amf.framework.document.Overlay())
}

@JSExportAll
class Extension(private[amf] val extensionFragment: amf.framework.document.Extension) extends Document(extensionFragment) {

  def this() = this(amf.framework.document.Extension())
}
