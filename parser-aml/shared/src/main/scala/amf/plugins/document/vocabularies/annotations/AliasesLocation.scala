package amf.plugins.document.vocabularies.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class AliasesLocation(position: Int) extends SerializableAnnotation {
  override val name: String = "aliases-location"
  override val value: String = position.toString
}

object AliasesLocation extends  AnnotationGraphLoader {
  override def unparse(annotationValue: String, objects: Map[String, AmfElement]) =
    AliasesLocation(Integer.parseInt(annotationValue))
}
