package amf.core.model.document

import amf.core.parser.{Annotations, Fields}

case class RecursiveUnit(fields: Fields, annotations: Annotations) extends Fragment

object RecursiveUnit {
  def apply(): RecursiveUnit = RecursiveUnit(Fields(), Annotations())
}
