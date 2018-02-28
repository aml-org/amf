package amf.core.parser

import amf.core.annotations.{LexicalInformation, SourceAST}
import amf.core.model.domain.{Annotation, SerializableAnnotation}
import org.yaml.model.YPart

/**
  * Element annotations
  */
class Annotations {
  private var annotations: Seq[Annotation] = Seq()

  def foreach(fn: (Annotation) => Unit): Unit = annotations.foreach(fn)

  def find[T <: Annotation](clazz: Class[T]): Option[T] = annotations.find(clazz.isInstance(_)).map(_.asInstanceOf[T])

  def contains[T <: Annotation](clazz: Class[T]): Boolean = find(clazz).isDefined

  def +=(annotation: Annotation): this.type = {
    annotations = annotations :+ annotation
    this
  }

  def ++=(other: Annotations): this.type = {
    annotations = annotations ++ other.annotations
    this
  }

  def reject(p: (Annotation) => Boolean): this.type = {
    annotations = annotations.filter(a => !p(a))
    this
  }

  /** Return [[SerializableAnnotation]]s only. */
  def serializables(): Seq[SerializableAnnotation] =
    annotations.filter(_.isInstanceOf[SerializableAnnotation]).map(_.asInstanceOf[SerializableAnnotation])

  def unapply[T <: Annotation](clazz: Class[T]): Option[T] = find(clazz)
}

object Annotations {

  def apply(): Annotations = new Annotations()

  def apply(ast: YPart): Annotations = apply() += LexicalInformation(Range(ast.range)) += SourceAST(ast)
}
