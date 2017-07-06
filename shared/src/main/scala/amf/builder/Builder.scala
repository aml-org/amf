package amf.builder

import amf.metadata.Field
import amf.model.{DomainElement, Fields}

/**
  * Created by martin.gutierrez on 6/29/17.
  */
trait Builder[T <: DomainElement[T, _]] {

  protected val fields: Fields = new Fields()

  protected def set(field: Field, value: Any): this.type = {
    fields set (field, value)
    this
  }

  def copy(fs: Fields): this.type = {
    fs.into(fields)
    this
  }

  def build: T
}
