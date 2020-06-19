package amf.client.model.document

import amf.plugins.document.webapi.model.{Extension => InternalExtension}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.document.Extension")
case class Extension(override private[amf] val _internal: InternalExtension) extends Document(_internal) {

  @JSExportTopLevel("model.document.Extension")
  def this() = this(InternalExtension())
}
