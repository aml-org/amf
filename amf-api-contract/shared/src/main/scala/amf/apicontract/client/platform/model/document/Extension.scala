package amf.apicontract.client.platform.model.document

import amf.apicontract.client.scala.model.document.{Extension => InternalExtension}
import amf.core.client.platform.model.document.Document

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Extension(override private[amf] val _internal: InternalExtension) extends Document(_internal) {

  @JSExportTopLevel("Extension")
  def this() = this(InternalExtension())
}
