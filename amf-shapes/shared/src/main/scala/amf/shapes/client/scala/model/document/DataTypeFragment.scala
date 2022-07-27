package amf.shapes.client.scala.model.document

import amf.core.client.scala.model.document.Fragment
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.document.metamodel.DataTypeFragmentModel

class DataTypeFragment(val fields: Fields, val annotations: Annotations) extends Fragment {
  override def encodes: Shape = super.encodes.asInstanceOf[Shape]

  /** Meta data for the document */
  override def meta: DataTypeFragmentModel.type = DataTypeFragmentModel
}

object DataTypeFragment {
  def apply(): DataTypeFragment = apply(Annotations())

  def apply(annotations: Annotations): DataTypeFragment = new DataTypeFragment(Fields(), annotations)
}
