package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Document model class.
  */
@JSExportAll
case class Document(private[amf] val document: amf.document.BaseUnit)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  def this() = this(amf.document.Document())

  def this(webApi: WebApi) = this(amf.document.Document().withEncodes(webApi.element))

  /** List of references to other [[DomainElement]]s. */
  override val references: js.Iterable[BaseUnit] =
    document.references.map(d => Document(d.asInstanceOf[amf.document.Document])).toJSArray

  /** Uri that identifies the document. */
  override val location: String = document.location

  /** Encoded [[DomainElement]] described in the document element. */
  val encodes: WebApi = {
    document match {
      case d: amf.document.Document => WebApi(d.encodes)
      case _                        => null
    }
  }

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: js.Iterable[amf.domain.DomainElement] = {
    document match {
      case d: amf.document.Document => d.declares.toJSArray
      case _                        => js.Array()
    }
  }

  override def unit: amf.document.BaseUnit = document
}
