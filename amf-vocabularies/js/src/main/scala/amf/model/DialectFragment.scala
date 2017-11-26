package amf.model

import amf.model.document.Fragment
import amf.plugins.document.vocabularies.model.document.{DialectFragment => CoreDialectFragment}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class DialectFragment(private[amf] val df: CoreDialectFragment) extends Fragment(df) {
  def this() = this(CoreDialectFragment())
}
