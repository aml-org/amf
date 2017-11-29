package amf.model.document

import amf.core.model.document

class Fragment(private[amf] val fragment: document.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment

}