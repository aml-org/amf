package amf.model.document

import amf.model.document
import amf.plugins.document.webapi.model

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Overlay(private[amf] val overlay: model.Overlay) extends document.Document(overlay) {
  @JSExportTopLevel("model.domain.Overlay")
  def this() = this(model.Overlay())
}
