package amf.model.document

import amf.plugins.document.webapi.model
import amf.model.document

class Overlay(private[amf] val overlay: model.Overlay) extends document.Document(overlay) {

  def this() = this(model.Overlay())
}
