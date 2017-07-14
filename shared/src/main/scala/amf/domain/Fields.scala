package amf.domain

import amf.metadata.Field
import amf.metadata.Type._
import amf.unsafe.PlatformSecrets

import scala.collection.immutable.ListMap

/**
  * Field values
  */
class Fields extends PlatformSecrets {

  private var fs: Map[Field, Value] = ListMap()

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

  def apply[T](field: Field): T = get(field)

  def foreach(fn: ((Field, Value)) => Unit): Unit = {
    fs.foreach(fn)
  }
}

case class Value(value: Any, annotations: List[Annotation]) {
  def cast[T](): T = value.asInstanceOf[T]
}
