package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
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

  /** List of references to other [[DomainElement]]s. */
  override val references: js.Iterable[BaseUnit] =
    document.references.map(d => Document(d.asInstanceOf[amf.document.Document])).toJSArray

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
