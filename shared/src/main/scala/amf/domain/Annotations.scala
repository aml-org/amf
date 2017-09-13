package amf.domain

import amf.common.AMFAST
import amf.domain.Annotation.{LexicalInformation, SourceAST}
import org.yaml.model.YPart

import scala.collection.mutable

/**
  * Element annotations
  */
class Annotations {
  private val annotations: mutable.ListBuffer[Annotation] = mutable.ListBuffer()

  def foreach(fn: (Annotation) => Unit): Unit = annotations.foreach(fn)

  def find[T <: Annotation](clazz: Class[T]): Option[T] = annotations.find(clazz.isInstance(_)).map(_.asInstanceOf[T])

  def contains[T <: Annotation](clazz: Class[T]): Boolean = find(clazz).isDefined

  def +=(annotation: Annotation): this.type = {
    annotations += annotation
    this
  }

  def ++=(other: Annotations): this.type = {
    annotations ++= other.annotations
    this
  }

  /** Return [[SerializableAnnotation]]s only. */
  def serializables(): Seq[SerializableAnnotation] =
    annotations.filter(_.isInstanceOf[SerializableAnnotation]).map(_.asInstanceOf[SerializableAnnotation])
}

object Annotations {

  def apply(): Annotations = new Annotations()

  //todo syaml
  def apply(ast: YPart): Annotations = apply() /*+= LexicalInformation(ast.range)*/ += SourceAST(ast)
}
