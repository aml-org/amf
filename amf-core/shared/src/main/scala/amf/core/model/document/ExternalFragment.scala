package amf.core.model.document

import amf.core.metamodel.Obj
import amf.core.metamodel.document.ExternalFragmentModel
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{Annotations, Fields}

class ExternalFragment(val fields: Fields, val annotations: Annotations) extends Fragment {
  override def encodes: ExternalDomainElement = super.encodes.asInstanceOf[ExternalDomainElement]

  /** Meta data for the document */
  override def meta: Obj = ExternalFragmentModel
}

object ExternalFragment {
  def apply(): ExternalFragment                         = apply(Annotations())
  def apply(annotations: Annotations): ExternalFragment = new ExternalFragment(Fields(), annotations)
}
