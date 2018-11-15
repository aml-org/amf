package amf.core.annotations
import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}

/** Used when a security scheme is null and not "null". */
case class NullSecurity() extends SerializableAnnotation {
  override val name: String  = "null-security"
  override val value: String = ""
}

object NullSecurity extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): Annotation = {
    NullSecurity()
  }
}
