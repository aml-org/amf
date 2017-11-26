package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class Aliases(aliases: Set[(String,String)]) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "aliases-array"

  /** Value as string. */
  override val value: String = aliases.map { case (alias, path) => s"$alias->$path" }.mkString(",")

}

object Aliases extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    Aliases(
      annotatedValue
        .split(",")
        .map(_.split("->") match {
          case Array(alias, url) => alias -> url
        })
        .toSet)
  }
}