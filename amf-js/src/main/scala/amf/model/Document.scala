package amf.model

import amf.domain.DomainElement
import amf.remote.URL

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * Document js class
  */
@JSExportAll
case class Document(private[amf] val document: amf.document.Document)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  /**
    * list of references to other [[DomainElement]]s
    */
  override val references: js.Iterable[BaseUnit] =
    document.references.map(d => Document(d.asInstanceOf[amf.document.Document])).toJSArray

  /**
    * Uri that identifies the document
    */
  override val location: URL = URL(document.location)

  /**
    * The parsing Unit that it's encoded for this Document
    */
  val encodes: WebApi = WebApi(document.encodes)

  /**
    *
    */
  val declares: js.Iterable[amf.domain.DomainElement] = document.declares.toJSArray
}
