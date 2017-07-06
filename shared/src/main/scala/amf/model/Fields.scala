package amf.model

import amf.metadata.Field

/**
  * Field values
  */
class Fields {

  private var fs: Map[Field, Any] = Map()

  def get[T](field: Field): T = fs(field).asInstanceOf[T]

  def set(field: Field, value: Any): this.type = {
    fs = fs + (field -> value)
    this
  }

  def into(other: Fields): Unit = fs = fs ++ other.fs
}
