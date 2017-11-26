package amf.plugins.document.vocabularies.model.document

import amf.core.metamodel.Obj
import amf.core.model.document.Fragment
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.document.DialectNodeFragmentModel

case class DialectFragment(fields: Fields, annotations: Annotations) extends Fragment {
  /** Meta data for the document */
  override def meta: Obj = DialectNodeFragmentModel
}

object DialectFragment {
  def apply(): DialectFragment = apply(Annotations())

  def apply(annotations: Annotations): DialectFragment = apply(Fields(), annotations)
}
