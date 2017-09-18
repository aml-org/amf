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
    .map({d =>
      d.asInstanceOf[amf.document.Document].encodes match {
        case w: amf.domain.WebApi => WebApi(w)
        case _ => throw new Exception("Vocabulary non supported in JS library yet")
      }
    }).get

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: js.Iterable[amf.domain.DomainElement] =
    document match {
      case d: amf.document.Document => d.declares.toJSArray
      case _                        => js.Array()
    }

  override def unit: amf.document.BaseUnit = document
}
