package amf.model

import amf.metadata.{Field, Type}
import amf.metadata.Type._

/**
  * Field values
  */
class Fields {

  private var fs: Map[Field, Any] = Map()

  def default(field: Field): Any = field.`type` match {
    case Array(e) => Nil
    case _        => null
  }

  def get[T](field: Field): T = {
    (fs.get(field) match {
      case Some(value) => value
      case _           => default(field)
    }).asInstanceOf[T]
  }

  def set(field: Field, value: Any): this.type = {
    fs = fs + (field -> value)
    this
  }

  def into(other: Fields): Unit = other.fs = other.fs ++ fs
}
