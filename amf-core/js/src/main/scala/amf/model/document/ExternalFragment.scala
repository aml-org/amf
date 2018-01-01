package amf.model.document

import amf.core.model.document.{ExternalFragment => SharedExternalFragment}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.document.ExternalFragment")
@JSExportAll
case class ExternalFragment(private[amf] val ef: SharedExternalFragment) extends Fragment(ef) {
  @JSExportTopLevel("model.document.ExternalFragment")
  def this() = this(SharedExternalFragment())
}