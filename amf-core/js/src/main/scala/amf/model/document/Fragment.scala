package amf.model.document

import amf.core.model.document

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class Fragment(private[amf] val fragment: document.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment
}