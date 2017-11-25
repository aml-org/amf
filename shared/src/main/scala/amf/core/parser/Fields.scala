package amf.core.parser

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.model.domain._
import amf.plugins.domain.shapes.models.UnresolvedShape

import scala.collection.immutable.ListMap

/**
  * Field values
  */
class Fields {

  private var fs: Map[Field, Value] = ListMap()

  def default(field: Field): AmfElement =
    Option(field.`type`).filter(_.isInstanceOf[Array]).map(_ => AmfArray(Nil)).orNull

  /** Return typed value associated to given [[Field]]. */
  def get(field: Field): AmfElement = {
    getValue(field) match {
      case Value(value, _) => value
      case _               => default(field)
    }
  }

  def fieldsMeta(): List[Field] = fs.keys.toList

  def ?[T](field: Field): Option[T] = fs.get(field).map(_.value.asInstanceOf[T])

  /** Return [[Value]] associated to given [[Field]]. */
  def getValue(field: Field): Value = fs.get(field).orNull

  def getAnnotation[T <: Annotation](field: Field, classType: Class[T]): Option[T] =
    fs.get(field).flatMap(_.annotations.find(classType))

  /** Add field array - value. */
  def add(id: String, field: Field, value: AmfElement): this.type = {
    adopt(id, value)
    ?[AmfArray](field) match {
      case Some(array) =>
        array += value
        this
      case None => set(id, field, AmfArray(Seq(value)))
    }
  }

  /** Add field array - value. */
  def addWithoutId(field: Field, value: AmfElement): this.type = {
    //adopt(id, value)
    ?[AmfArray](field) match {
      case Some(array) =>
        array += value
        this
      case None => setWithoutId(field, AmfArray(Seq(value)))
    }
  }

  /** Set field value entry-point. */
  def set(id: String, field: Field, value: AmfElement, annotations: Annotations = Annotations()): this.type = {
    if (field.value.iri() == "http://raml.org/vocabularies/document#declares") {
      // declaration, set correctly the id
      adopt(id + "#/declarations", value)
    } else {
      adopt(id, value)
    }
    fs = fs + (field -> Value(value, annotations))
    this
  }

  /** Set field value entry-point without adopting it. */
  def setWithoutId(field: Field, value: AmfElement, annotations: Annotations = Annotations()): this.type = {
    fs = fs + (field -> Value(value, annotations))
    this
  }

  def link(field: Field, value: AmfObject, annotations: Annotations): this.type = {
    fs = fs + (field -> Link(value, annotations))
    this
  }

  def remove(field: Field): this.type = {
    fs = fs - field
    this
  }

  def into(other: Fields): Unit = {
    // TODO array copy with references instead of instance
    other.fs = other.fs ++ fs
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

  def entryJsonld(f: Field): Option[FieldEntry] = {
    if (f.jsonldField) {
      entry(f)
    } else {
      None
    }
  }

  def foreach(fn: ((Field, Value)) => Unit): Unit = {
    fs.foreach(fn)
  }

  def filter(fn: ((Field, Value)) => Boolean): Fields = {
    fs = fs.filter(fn)
    this
  }

  private def adopt(id: String, value: AmfElement): Unit = value match {
    case obj: AmfObject => obj.adopted(id)
    case seq: AmfArray  => seq.values.foreach(adopt(id, _))
    case _              => // Do nothing with scalars
  }

  def fields(): Iterable[FieldEntry] = fs.map(FieldEntry.tupled)

  def size: Int = fs.size

  def nonEmpty: Boolean = fs.nonEmpty
}

object Fields {
  def apply(): Fields = new Fields()
}

class Value(var value: AmfElement, val annotations: Annotations) {

  // Values are going to *mutate* automatically when references to unresolved values are resolved in declarations
  checkUnresolved()

  override def toString: String = value.toString

  def checkUnresolved(): Unit = value match {
    case unresolved: UnresolvedShape =>
      // This callback will be registered in the declarations of the parser context to be executed when a reference is resolved
      unresolved.futureRef((resolved) => {
        value = resolved.link(unresolved.reference, unresolved.annotations)
      })

    case array: AmfArray => // Same for arrays, but iterating through elements and looking for unresolved
      array.values.foreach {
        case unresolved: UnresolvedShape =>
          unresolved.futureRef((resolved) => {
            val updated = value.asInstanceOf[AmfArray].values.map {
              case x if x == unresolved => resolved.link(unresolved.reference, unresolved.annotations)
              case other                => other
            }
            value = AmfArray(updated, value.annotations)
          })
        case _ => // ignore
      }
    case _ => // ignore
  }
}

object Value {
  def apply(value: AmfElement, annotations: Annotations) = new Value(value, annotations)

  def unapply(link: Value): Option[(AmfElement, Annotations)] =
    if (Option(link).isDefined)
      Some((link.value, link.annotations))
    else
      None
}

class Link(value: AmfObject, annotations: Annotations) extends Value(value, annotations) {
  override def toString: String = value.id
}

object Link {
  def apply(value: AmfObject, annotations: Annotations) = new Link(value, annotations)
}

case class FieldEntry(field: Field, value: Value) {
  def element: AmfElement = value.value

  def scalar: AmfScalar = element.asInstanceOf[AmfScalar]

  def array: AmfArray = element.asInstanceOf[AmfArray]

  def obj: AmfObject = element.asInstanceOf[AmfObject]

  def negated: FieldEntry = copy(value = Value(AmfScalar(!scalar.toBool, element.annotations), value.annotations))

  def domainElement: DomainElement = element.asInstanceOf[DomainElement]

  def isLink: Boolean = value.isInstanceOf[Link]
}
