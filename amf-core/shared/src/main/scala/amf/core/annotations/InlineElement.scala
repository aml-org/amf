package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class InlineElement() extends SerializableAnnotation {
  override val name: String = "inline-element"

  override val value: String = ""
}

object InlineElement extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    InlineElement()
  }
}

case class LocalElement() extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "local-element"

  /** Value as string. */
  override val value: String = ""
}

object LocalElement extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    LocalElement()
  }
}
