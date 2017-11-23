package amf.model

/**
  * JVM Document model class.
  */
case class Document(private[amf] val document: amf.framework.model.document.Document)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  def this() = this(amf.framework.model.document.Document())

  def this(webApi: WebApi) = this(amf.framework.model.document.Document().withEncodes(webApi.element))

  def resolve(profile: String): Document = Document(document.resolve(profile))

  override private[amf] val element = document
}

class Overlay(private[amf] val overlay: amf.framework.model.document.Overlay) extends Document(overlay) {

  def this() = this(amf.framework.model.document.Overlay())
}

class Extension(private[amf] val extensionFragment: amf.framework.model.document.Extension) extends Document(extensionFragment) {

  def this() = this(amf.framework.model.document.Extension())
}
