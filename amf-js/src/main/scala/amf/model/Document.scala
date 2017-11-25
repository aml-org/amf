package amf.model

import amf.plugins.document.webapi.model.{Extension => CoreExtension, Overlay => CoreOverlay}
import amf.core.model.document.{Document => CoreDocument}

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}

/**
  * JS Document model class.
  */
@JSExportAll
case class Document(private[amf] val document: CoreDocument)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  @JSExportTopLevel("Document")
  def this() = this(CoreDocument())

  @JSExportTopLevel("Document")
  def this(webApi: WebApi) = this(CoreDocument().withEncodes(webApi.element))

  @JSExport
  def resolve(profile: String): Document = Document(document.resolve(profile))

  override private[amf] val element = document
}

@JSExportAll
class Overlay(private[amf] val overlay: CoreOverlay) extends Document(overlay) {

  def this() = this(CoreOverlay())
}

@JSExportAll
class Extension(private[amf] val extensionFragment: CoreExtension) extends Document(extensionFragment) {

  def this() = this(CoreExtension())
}
