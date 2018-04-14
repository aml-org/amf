package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class ExternalSourceAnnotation(oriId: String, oriLabel: String) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "external-source"

  /** Value as string. */
  override val value: String = oriLabel + "->" + oriId
}

object ExternalSourceAnnotation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    annotatedValue.split("->") match {
      case Array(oriLabel, oriId) => ExternalSourceAnnotation(oriId, oriLabel)
      case _                      => ExternalSourceAnnotation("", "") // ??
    }
  }
}
