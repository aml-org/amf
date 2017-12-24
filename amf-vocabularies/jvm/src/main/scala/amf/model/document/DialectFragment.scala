package amf.model.document

import amf.plugins.document.vocabularies.model.document.{DialectFragment => CoreDialectFragment}

class DialectFragment(private[amf] val df: CoreDialectFragment) extends Fragment(df) {
  def this() = this(CoreDialectFragment())
}
