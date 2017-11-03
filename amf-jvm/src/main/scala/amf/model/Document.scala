package amf.model

/**
  * JVM Document model class.
  */
case class Document(private[amf] val document: amf.document.Document)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  def this() = this(amf.document.Document())

  def this(webApi: WebApi) = this(amf.document.Document().withEncodes(webApi.element))

  def resolve(profile: String): Document = Document(document.resolve(profile))

  override private[amf] val element = document
}
