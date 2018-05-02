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

case class InheritedShapes(baseIds: Seq[String]) extends  SerializableAnnotation {
  override val name: String  = "inherited-shapes"
  override val value: String = baseIds.mkString(",")
}

object InheritedShapes extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    InheritedShapes(annotatedValue.split(","))
  }
}

case class NilUnion(position: String) extends  SerializableAnnotation {
  override val name: String  = "nil-union"
  override val value: String = position
}

object NilUnion extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    NilUnion(annotatedValue)
  }
}