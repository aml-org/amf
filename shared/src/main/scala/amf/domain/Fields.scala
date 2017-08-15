package amf.domain

import amf.domain.Annotation.ArrayFieldAnnotations
import amf.metadata.Field
import amf.metadata.Type._
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.unsafe.PlatformSecrets

import scala.collection.immutable.ListMap

/**
  * Field values
  */
class Fields extends PlatformSecrets {

  private var fs: Map[Field, Value] = ListMap()
  var id: String                    = _

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

  def ?[T](field: Field): Option[T] = fs.get(field).map(_.value.asInstanceOf[T])

  /** Return [[Value]] associated to given [[Field]]. */
  def getValue(field: Field): Value = {
    fs.get(field) match {
      case Some(value) => value
      case _           => null
    }
  }

  def getAnnotation[T <: Annotation](field: Field, classType: Class[T]): Option[T] = {
    fs.get(field).flatMap(_.annotations.find(classType.isInstance(_))).asInstanceOf[Option[T]]
  }

  def getAnnotationForValue[T <: Annotation](field: Field, value: Any, classType: Class[T]): Option[T] = {
    getAnnotation(field, classOf[ArrayFieldAnnotations])
      .flatMap(_(value).find(classType.isInstance(_)))
      .asInstanceOf[Option[T]]
  }

  def set(field: Field, values: Seq[AmfElement]): this.type = {
    fs = fs + (field -> values)
    this
  }

  def add(field: Field, value: AmfElement): AmfArray = {}

  def array(field: Field): AmfArray = {
    fs.get(field) match {
      case Some(Value(value, _)) => value.asInstanceOf[AmfArray]
      case None => {
        set(field, AmfArray(Nil))
      }
    }
  }

  def set(field: Field, value: AmfElement, annotations: Annotations = Annotations()): this.type = {
    fs = fs + (field -> Value(value, annotations))
    this
  }

  def into(other: Fields): Unit = {
    //TODO array copy with references instead of instance
    other.fs = other.fs ++ fs
    other.id = id
  }

  def apply[T](field: Field): T = get(field)

  /** Return optional entry for a given [[Field]]. */
  def entry(f: Field): Option[(Field, Value)] = {
    fs.get(f) match {
      case Some(value) => Some((f, value))
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

}

object Fields {
  def apply(): Fields = new Fields()
}

case class Value(value: AmfElement, annotations: Annotations)
