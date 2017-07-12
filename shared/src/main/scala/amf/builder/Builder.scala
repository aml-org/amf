package amf.builder

import amf.metadata.Field
import amf.model.{Annotation, DomainElement, Fields}

/**
  * Created by martin.gutierrez on 6/29/17.
  */
trait Builder[T <: DomainElement[T, _]] {

  protected val fields: Fields = new Fields()

  def set(field: Field, value: Any, annotations: List[Annotation] = Nil): this.type = {
    fields set (field, value, annotations)
    this
  }

  def copy(fs: Fields): this.type = {
    fs.into(fields)
    this
  }

  def build: T
}
