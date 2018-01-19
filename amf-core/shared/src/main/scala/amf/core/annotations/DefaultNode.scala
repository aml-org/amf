package amf.core.annotations

import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}

/**
  * Node generated during parsing that should not be taken into consideration for resolution
  */
case class DefaultNode() extends SerializableAnnotation {
  override val name: String = "default-node"
  override val value: String = ""
}

object DefaultNode extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): Annotation = {
    DefaultNode()
  }
}