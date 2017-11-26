package amf.model

import amf.model.document.Fragment
import amf.plugins.document.vocabularies.model.document.{DialectFragment => CoreDialectFragment}

class DialectFragment(private[amf] val df: CoreDialectFragment) extends Fragment(df) {
  def this() = this(CoreDialectFragment())
}
