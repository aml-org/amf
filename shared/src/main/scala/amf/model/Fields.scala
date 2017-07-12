package amf.model

import amf.metadata.Field
import amf.metadata.Type._

/**
  * Field values
  */
class Fields {

  private var fs: Map[Field, Value] = Map()

  def default(field: Field): Any = field.`type` match {
    case Array(_) => Nil
    case _        => null
  }

  /** Return typed value associated to given [[Field]]. */
  def get[T](field: Field): T = {
    (getValue(field) match {
      case Value(value, _) => value
      case _               => default(field)
    }).asInstanceOf[T]
  }

  /** Return [[Value]] associated to given [[Field]]. */
  def getValue(field: Field): Value = {
    fs.get(field) match {
      case Some(value) => value
      case _           => null
    }
  }

  def set(field: Field, value: Any, annotations: List[Annotation]): this.type = {
    fs = fs + (field -> Value(value, annotations))
    this
  }

  def into(other: Fields): Unit = other.fs = other.fs ++ fs

}

case class Value(value: Any, annotations: List[Annotation]) {
  def cast[T](): T = value.asInstanceOf[T]
}
