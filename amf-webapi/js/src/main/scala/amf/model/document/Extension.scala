package amf.model.document

import amf.plugins.document.webapi.model

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Extension(private[amf] val extensionFragment: model.Extension) extends Document(extensionFragment) {
  @JSExportTopLevel("model.domain.Extension")
  def this() = this(model.Extension())
}
