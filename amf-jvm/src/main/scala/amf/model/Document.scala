package amf.model

import amf.core.model.document.{Document => CoreDocument}
import amf.plugins.document.webapi.model.{Overlay => CoreOverlay, Extension => CoreExtension}

/**
  * JVM Document model class.
  */
case class Document(private[amf] val document: CoreDocument)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  def this() = this(amf.core.model.document.Document())

  def this(webApi: WebApi) = this(amf.core.model.document.Document().withEncodes(webApi.element))

  def resolve(profile: String): Document = Document(document.resolve(profile))

  override private[amf] val element = document
}

class Overlay(private[amf] val overlay: CoreOverlay) extends Document(overlay) {

  def this() = this(CoreOverlay())
}

class Extension(private[amf] val extensionFragment: CoreExtension) extends Document(extensionFragment) {

  def this() = this(CoreExtension())
}
