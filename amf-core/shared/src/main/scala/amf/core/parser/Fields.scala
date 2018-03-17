package amf.core.parser

import amf.client.model._
import amf.core.metamodel.Type._
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.model.domain._

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

  def field[T](f: Field): T = {

    def typed(t: Type, e: AmfElement): Any = e match {
      case s: AmfScalar =>
        t match {
          case Str | Iri   => new StrFieldImpl(s, defined())
          case Bool        => new BoolFieldImpl(s, defined())
          case Type.Int    => new IntFieldImpl(s, defined())
          case Type.Float  => new DoubleFieldImpl(s, defined())
          case Type.Double => new DoubleFieldImpl(s, defined())
          case Type.Any    => new AnyFieldImpl(s, defined())
          case _           => throw new Exception(s"Invalid value '$s' of type '$t'")
        }
      case o: AmfObject =>
        t match {
          case _: Obj => o
          case _      => throw new Exception(s"Invalid value '$o' of type '$t'")
        }
      case a: AmfArray =>
        t match {
          case ArrayLike(element) => a.values.map(typed(element, _))
          case _                  => throw new Exception(s"Invalid value '$a' of type '$t'")
        }
    }

    //noinspection ScalaStyle
    def empty(): T =
      (f.`type` match {
        case Str | Iri    => StrFieldImpl(null, Annotations(), present = false)
        case Bool         => BoolFieldImpl(null.asInstanceOf[Boolean], Annotations(), present = false)
        case Type.Int     => IntFieldImpl(null.asInstanceOf[Int], Annotations(), present = false)
        case Type.Float   => DoubleFieldImpl(null.asInstanceOf[Double], Annotations(), present = false)
        case Type.Double  => DoubleFieldImpl(null.asInstanceOf[Double], Annotations(), present = false)
        case Type.Any     => AnyFieldImpl(null, Annotations(), defined())
        case ArrayLike(_) => Nil
        case _: Obj       => null
      }).asInstanceOf[T]

    def defined(): Boolean = fs.get(f).isDefined

    fs.get(f).map(v => typed(f.`type`, v.value)).fold(empty())(_.asInstanceOf[T])
  }

  def fieldsMeta(): List[Field] = fs.keys.toList

  def ?[T](field: Field): Option[T] = fs.get(field).map(_.value.asInstanceOf[T])

  /** Return [[Value]] associated to given [[Field]]. */
  def getValue(field: Field): Value = fs.get(field).orNull

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

  private case class StrFieldImpl(value: String, annotations: Annotations, present: Boolean) extends StrField {
    def this(s: AmfScalar, present: Boolean) = this(s.value.asInstanceOf[String], s.annotations, present)
  }

  private case class BoolFieldImpl(value: Boolean, annotations: Annotations, present: Boolean) extends BoolField {
    def this(s: AmfScalar, present: Boolean) = this(s.value.asInstanceOf[Boolean], s.annotations, present)
  }

  private case class IntFieldImpl(value: Int, annotations: Annotations, present: Boolean) extends IntField {
    def this(s: AmfScalar, present: Boolean) = this(s.value.asInstanceOf[Int], s.annotations, present)
  }

  private case class DoubleFieldImpl(value: Double, annotations: Annotations, present: Boolean) extends DoubleField {
    def this(s: AmfScalar, present: Boolean) = this(s.value.asInstanceOf[Double], s.annotations, present)
  }

  private case class AnyFieldImpl(value: Any, annotations: Annotations, present: Boolean) extends AnyField {
    def this(s: AmfScalar, present: Boolean) = this(s.value, s.annotations, present)
  }
}

object Fields {
  def apply(): Fields = new Fields()
}

class Value(var value: AmfElement, val annotations: Annotations) {

  // Values are going to *mutate* automatically when references to unresolved values are resolved in declarations
  checkUnresolved()

  override def toString: String = value.toString

  def checkUnresolved() = value match {
    case linkable: Linkable if linkable.isUnresolved =>
      // this is a callback that will be registered
      // in the declarations of the parser context
      // to be executed when a reference is resolved
      linkable.toFutureRef((resolved) => {
        value = resolved.resolveUnreferencedLink(linkable.refName, linkable.annotations, linkable) // mutation of the field value
      })

    case array: AmfArray => // Same for arrays, but iterating through elements and looking for unresolved
      array.values.foreach {
        case linkable: Linkable if linkable.isUnresolved =>
          linkable.toFutureRef((resolved) => {
            value.asInstanceOf[AmfArray].values = value.asInstanceOf[AmfArray].values map { element =>
              if (element == linkable) {
                resolved.resolveUnreferencedLink(linkable.refName, linkable.annotations, element) // mutation of the field value
              } else {
                element
              }
            }
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
