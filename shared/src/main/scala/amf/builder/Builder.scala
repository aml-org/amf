package amf.builder

import amf.domain.{Annotation, DomainElement, Fields}
import amf.metadata.{Field, Type}

/**
  * Builder for [[DomainElement]]s
  */
trait Builder[T <: DomainElement] {

  protected val fields: Fields = new Fields()

  def set(field: Field, value: Any, annotations: List[Annotation] = Nil): this.type = {
    fields set (field, value, annotations)
    this
  }

  def add(field: Field, value: Any, annotations: List[Annotation] = Nil): this.type = {
    if (field.`type`.isInstanceOf[Type.Array]) {
      val elements: Seq[_]  = fields get field
      val castValue: Seq[_] = value.asInstanceOf[List[_]]
      fields set (field, elements ++ castValue, annotations)
    } else {
      //Illegal!
    }
    this
  }

  protected def copy(fs: Fields): this.type = {
    fs.into(fields)
    this
  }

  def build: T
}
