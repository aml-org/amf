package amf.apicontract.client.platform.model.document

import amf.apicontract.client.scala.model.document.{Overlay => InternalOverlay}
import amf.core.client.platform.model.document.Document

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Overlay(override private[amf] val _internal: InternalOverlay) extends Document(_internal) {

  @JSExportTopLevel("Overlay")
  def this() = this(InternalOverlay())
}
