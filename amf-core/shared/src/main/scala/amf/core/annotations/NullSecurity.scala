package amf.core.annotations
import amf.core.model.domain._

/** Used when a security scheme is null and not "null". */
case class NullSecurity() extends SerializableAnnotation {
  override val name: String  = "null-security"
  override val value: String = ""
}

object NullSecurity extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = Some(NullSecurity())
}
