package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * Document js class
  */
@JSExportAll
case class Document(private[amf] val document: amf.document.BaseUnit)
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
  override val location: String = document.location

  /**
    * The parsing Unit that it's encoded for this [[Document]]
    */
  val encodes: WebApi = {
    document match {
      case document1: amf.document.Document => WebApi(document1.encodes)
      case _                                => null
    }
  }

  /**
    *
    */
  val declares: js.Iterable[amf.domain.DomainElement] = {
    document match {
      case document1: amf.document.Document => document1.declares.toJSArray
      case _                                => js.Array()
    }
  }

  override def unit: amf.document.BaseUnit = document
}
