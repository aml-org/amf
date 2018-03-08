package amf.client.model.document

import amf.plugins.document.webapi.model.{Overlay => InternalOverlay}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.Overlay")
case class Overlay(override private[amf] val _internal: InternalOverlay) extends Document(_internal) {

  @JSExportTopLevel("model.domain.Overlay")
  def this() = this(InternalOverlay())
}
