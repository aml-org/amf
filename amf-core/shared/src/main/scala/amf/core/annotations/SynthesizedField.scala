package amf.core.annotations

import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}

case class SynthesizedField() extends SerializableAnnotation {
  override val name: String  = "synthesized-field"
  override val value: String = "true"
}

object SynthesizedField extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(SynthesizedField())
}