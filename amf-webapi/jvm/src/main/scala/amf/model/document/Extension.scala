package amf.model.document

import amf.plugins.document.webapi.model

class Extension(private[amf] val extensionFragment: model.Extension) extends Document(extensionFragment) {

  def this() = this(model.Extension())
}
