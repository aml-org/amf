package amf.model.document

import amf.plugins.document.webapi.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class Extension(private[amf] val extensionFragment: model.Extension) extends Document(extensionFragment) {

  def this() = this(model.Extension())
}
