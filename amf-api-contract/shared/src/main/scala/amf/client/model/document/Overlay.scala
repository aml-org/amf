package amf.client.model.document

import amf.core.client.platform.model.document.Document
import amf.plugins.document.apicontract.model.{Overlay => InternalOverlay}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Overlay(override private[amf] val _internal: InternalOverlay) extends Document(_internal) {

  @JSExportTopLevel("model.domain.Overlay")
  def this() = this(InternalOverlay())
}
