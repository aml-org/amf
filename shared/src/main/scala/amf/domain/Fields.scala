package amf.domain

import amf.metadata.Field
import amf.metadata.Type._
import amf.model.{AmfArray, AmfElement, AmfObject, AmfScalar}

import scala.collection.immutable.ListMap

/**
  * Field values
  */
class Fields {

  private var fs: Map[Field, Value] = ListMap()
  var id: String                    = _

  def default(field: Field): AmfElement = field.`type` match {
    case Array(_) => AmfArray(Nil)
    case _        => null
  }

  /** Return typed value associated to given [[Field]]. */
  def get(field: Field): AmfElement = {
    getValue(field) match {
      case Value(value, _) => value
      case _               => default(field)
    }
  }

  def ?[T](field: Field): Option[T] = fs.get(field).map(_.value.asInstanceOf[T])

  /** Return [[Value]] associated to given [[Field]]. */
  def getValue(field: Field): Value = {
    fs.get(field) match {
      case Some(value) => value
      case _           => null
    }
  }

  def getAnnotation[T <: Annotation](field: Field, classType: Class[T]): Option[T] =
    fs.get(field).flatMap(_.annotations.find(classType))

  /** Set field value entry-point. */
  def set(field: Field, value: AmfElement, annotations: Annotations = Annotations()): this.type = {
    adopt(value)
    fs = fs + (field -> Value(value, annotations))
    this
  }

  def into(other: Fields): Unit = {
    //TODO array copy with references instead of instance
    other.fs = other.fs ++ fs
    other.id = id
  }

  def apply[T](field: Field): T = rawAny(get(field))

  def raw[T](field: Field): Option[T] = getValue(field) match {
    case Value(value, _) => Some(rawAny(value))
    case _               => None
  }

  private def rawAny[T](element: AmfElement): T = {
    (element match {
      case AmfArray(values, _) => values.map(rawAny[T])
      case AmfScalar(value, _) => value
      case obj                 => obj
    }).asInstanceOf[T]
  }

  /** Return optional entry for a given [[Field]]. */
  def entry(f: Field): Option[FieldEntry] = {
    fs.get(f) match {
      case Some(value) => Some(FieldEntry(f, value))
      case _           => None
    }
  }

  def foreach(fn: ((Field, Value)) => Unit): Unit = {
    fs.foreach(fn)
  }

  def filter(fn: ((Field, Value)) => Boolean): Fields = {
    fs = fs.filter(fn)
    this
  }

  private def adopt(value: AmfElement): Unit = value match {
    case obj: AmfObject => obj.adopted(id)
    case seq: AmfArray  => seq.values.foreach(adopt)
    case _              => // Do nothing with scalars
  }

  def fields(): Iterable[FieldEntry] = fs.map(FieldEntry.tupled)

  def size: Int = fs.size

  def nonEmpty: Boolean = fs.nonEmpty
}

object Fields {
  def apply(): Fields = new Fields()
}

case class Value(value: AmfElement, annotations: Annotations) {
  override def toString: String = value.toString
}

case class FieldEntry(field: Field, value: Value)
