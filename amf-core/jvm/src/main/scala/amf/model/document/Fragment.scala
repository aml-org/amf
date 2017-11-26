package amf.model.document

import amf.core.model.document

case class Fragment(private[amf] val fragment: document.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment

}