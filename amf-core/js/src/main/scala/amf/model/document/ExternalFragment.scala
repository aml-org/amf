package amf.model.document

import amf.core.model.document.{ExternalFragment => SharedExternalFragment}
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class ExternalFragment(private[amf] val ef: SharedExternalFragment) extends Fragment(ef) {
  def this() = this(SharedExternalFragment())
}