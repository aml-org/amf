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

  def collect[T <: Annotation](pf: PartialFunction[Annotation, T]): Seq[T] = annotations.collect(pf)

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
  def serializables(): Seq[SerializableAnnotation] = collect { case s: SerializableAnnotation => s }

  def unapply[T <: Annotation](clazz: Class[T]): Option[T] = find(clazz)
}

object Annotations {

  def apply(): Annotations = new Annotations()

  def apply(annotations: Annotations): Annotations = {
    val result = apply()
    result.annotations = annotations.annotations.map(identity)
    result
  }

  def apply(ast: YPart): Annotations = apply() += LexicalInformation(Range(ast.range)) += SourceAST(ast)

  def apply(annotations: Seq[Annotation]): Annotations = {
    val result = apply()
    result.annotations = annotations
    result
  }
}
