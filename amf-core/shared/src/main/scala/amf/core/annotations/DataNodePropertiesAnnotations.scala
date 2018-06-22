package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class DataNodePropertiesAnnotations(properties: Map[String, LexicalInformation]) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "data-node-properties"

  /** Value as string. */
  override val value: String = properties
    .map {
      case (key, l) => s"$key->${l.range}"
    }
    .mkString("#")
}

object DataNodePropertiesAnnotations extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    val tuples: Array[(String, LexicalInformation)] = annotatedValue
      .split("#")
      .map(_.split("->") match {
        case Array(key, range) => key -> LexicalInformation(range)
      })
    DataNodePropertiesAnnotations(tuples.toMap)
  }
}
