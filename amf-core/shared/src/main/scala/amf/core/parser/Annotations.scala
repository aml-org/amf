package amf.core.parser

import amf.core.annotations.{LexicalInformation, SourceAST, SourceLocation, SourceNode}
import amf.core.model.domain.{Annotation, EternalSerializedAnnotation, SerializableAnnotation}
import org.yaml.model.{YMapEntry, YNode, YPart}

import scala.collection.mutable.ArrayBuffer

/**
  * Element annotations
  */
class Annotations(hintSize: Int = 4) {

  private var annotations: ArrayBuffer[Annotation] = new ArrayBuffer(hintSize)

  def foreach(fn: Annotation => Unit): Unit = annotations.foreach(fn)

  def find[T <: Annotation](fn: Annotation => Boolean): Option[T] = annotations.find(fn).map(_.asInstanceOf[T])

  def find[T <: Annotation](clazz: Class[T]): Option[T] = find(clazz.isInstance(_))

  def collect[T <: Annotation](pf: PartialFunction[Annotation, T]): Seq[T] = annotations.collect(pf)

  def contains[T <: Annotation](clazz: Class[T]): Boolean = find(clazz).isDefined

  def size: Int = annotations.size

  def +=(annotation: Annotation): this.type = {
    annotations += annotation
    this
  }

  def ++=(other: Annotations): this.type = this ++= other.annotations

  def ++=(other: TraversableOnce[Annotation]): this.type = {
    annotations ++= other
    this
  }

  def reject(p: Annotation => Boolean): this.type = {
    annotations = annotations.filter(a => !p(a))
    this
  }

  /** Return [[SerializableAnnotation]]s only. */
  def serializables(): Seq[SerializableAnnotation] = collect {
    case s: SerializableAnnotation if !s.isInstanceOf[EternalSerializedAnnotation] => s
  }

  /** Return [[EternalSerializedAnnotation]]s only. */
  def eternals(): Seq[EternalSerializedAnnotation] = collect { case e: EternalSerializedAnnotation => e }

  def unapply[T <: Annotation](clazz: Class[T]): Option[T] = find(clazz)

  def copy(): Annotations = Annotations(this)
}

object Annotations {

  def apply(): Annotations = new Annotations()

  def apply(annotations: Annotations): Annotations = {
    val result = new Annotations(annotations.size)
    result.annotations ++= annotations.annotations
    result
  }

  def apply(ast: YPart): Annotations = {
    val annotations = new Annotations() ++= Set(LexicalInformation(Range(ast.range)),
                                                SourceAST(ast),
                                                SourceLocation(ast.sourceName))
    ast match {
      case node: YNode      => annotations += SourceNode(node)
      case entry: YMapEntry => annotations += SourceNode(entry.value)
      case _                => annotations
    }

  }

  // todo: temp method to keep compatibility against previous range serializacion logic.
  // We should discuss if always use the range of the YNode, or always use the range of the ynode member.
  def valueNode(node: YNode): Annotations = apply(node.value) += SourceNode(node)

  def apply(annotation: Annotation): Annotations = new Annotations() += annotation
}
