package amf.apicontract.internal.annotations

import amf.core.client.scala.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class ExtendsReference(ref: String) extends SerializableAnnotation {
  /** Extension name. */
  override val name: String = "extends-reference"

  /** Value as string. */
  override def value: String = ref
}


object ExtendsReference extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[ExtendsReference] = {
    Some(ExtendsReference(value))
  }
}
