package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}
case class Aliases(aliases: Set[(Aliases.Alias,(Aliases.FullUrl, Aliases.RelativeUrl))]) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "aliases-array"

  /** Value as string. */
  override val value: String = aliases.map { case (alias, (fullUrl, relativeUrl)) => s"$alias->$fullUrl::$relativeUrl" }.mkString(",")

}

object Aliases extends AnnotationGraphLoader {

  type FullUrl = String
  type RelativeUrl = String
  type Alias = String

  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    Aliases(
      annotatedValue
        .split(",")
        .map(_.split("->") match {
          case Array(alias, urls) =>
            urls.split("::") match {
              case Array(fullUrl, relativeUrl) =>
                alias -> (fullUrl, relativeUrl)
            }
        })
        .toSet)
  }
}