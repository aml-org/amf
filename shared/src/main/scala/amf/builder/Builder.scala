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

  private def arrayAnnotation(field: Field): ArrayFieldAnnotations = {
    fields.getAnnotation(field, classOf[ArrayFieldAnnotations]).getOrElse(ArrayFieldAnnotations())
  }

  protected def copy(fs: Fields): this.type = {
    fs.into(fields)
    this
  }

  def build: T
}
