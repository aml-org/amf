package amf.builder

import amf.document.BaseUnit
import amf.domain.Annotation.ArrayFieldAnnotations
import amf.domain.{Annotation, DomainElement, Fields}
import amf.metadata.{Field, Type}

/**
  * Builder for [[DomainElement]]s and [[BaseUnit]]s
  */
trait Builder {

  type T

  protected val fields: Fields = new Fields()

  protected var annotations: List[Annotation] = Nil

  def set(field: Field, value: Any, annotations: List[Annotation] = Nil): this.type = {
    fields.set(field, value, annotations)
    this
  }

  def add(field: Field, value: Any, annotations: List[Annotation] = Nil): this.type = {
    if (field.`type`.isInstanceOf[Type.Array]) {
      val elements: Seq[_] = fields(field)
      val castValue: Seq[_] = value match {
        case _: List[_] => value.asInstanceOf[List[_]]
        case _          => List(value)
      }

      fields.set(field, elements ++ castValue, List(arrayAnnotation(field) + (value, annotations)))
    } else {
      //Illegal!
    }
    this
  }

  /** Set id. */
  def withId(id: String): this.type = {
    fields.id = id
    this
  }

  /** Resolve id given container id. */
  def resolveId(container: String): this.type

  def getId: String = fields.id

  private def arrayAnnotation(field: Field): ArrayFieldAnnotations = {
    fields.getAnnotation(field, classOf[ArrayFieldAnnotations]).getOrElse(ArrayFieldAnnotations())
  }

  protected def copy(fs: Fields): this.type = {
    fs.into(fields)
    this
  }

  protected def withAnnotations(as: List[Annotation]): this.type = {
    annotations = as
    this
  }

  def build: T
}
