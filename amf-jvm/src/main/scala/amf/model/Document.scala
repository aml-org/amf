package amf.model

import scala.collection.JavaConverters._

/**
  * JVM Document model class.
  */
case class Document(private[amf] val document: amf.document.BaseUnit)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  def this() = this(amf.document.Document())

  def this(webApi: WebApi) = this(amf.document.Document().withEncodes(webApi.element))

  /** List of references to other [[DomainElement]]s. */
  override val references: java.util.List[BaseUnit] =
    document.references
      .map({ case r: amf.model.Document => Document(r) })
      .map({ bu: BaseUnit =>
        bu
      })
      .asJava

  /** Uri that identifies the document. */
  override val location: String = document.location

  /** Encoded [[DomainElement]] described in the document element. */
  val encodes: WebApi = Option(document)
    .filter(_.isInstanceOf[amf.document.Document])
    .map(d => WebApi(d.asInstanceOf[amf.document.Document].encodes))
    .orNull

  override def unit: amf.document.BaseUnit = document

  override def usage: String = document.usage

  override private[amf] def element = document.asInstanceOf[amf.document.DeclaresModel]
}
