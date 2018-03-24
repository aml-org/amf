package amf.plugins.domain.shapes.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class InheritanceProvenance(baseId: String) extends  SerializableAnnotation {
  override val name: String  = "inheritance-provenance"
  override val value: String = baseId
}

object InheritanceProvenance extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    InheritanceProvenance(annotatedValue)
  }
}
