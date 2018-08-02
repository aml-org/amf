package amf.core.metamodel

import amf.core.vocabulary.Namespace.Xsd
import amf.core.vocabulary.ValueType

/**
  *
  */
trait Type {
  val `type`: List[ValueType]

  val dynamic: Boolean = false
}

trait Obj extends Type {

  // This can be override by dynamic element models to provide
  // the information about fields at parsing time

  override val dynamic: Boolean = false

  def fields: List[Field]
}

object Type {

  case class Scalar(id: String) extends Type {
    override val `type`: List[ValueType] = List(Xsd + id)
  }

  object Str extends Scalar("string")

  object RegExp extends Scalar("regexp")

  object Int extends Scalar("int")

  object Float extends Scalar("float")

  object Double extends Scalar("double")

  object Time extends Scalar("time")

  object Date extends Scalar("date")

  object DateTime extends Scalar("dateTime")

  object Iri extends Scalar("url")

  object EncodedIri extends Scalar("encodedUrl")

  object Bool extends Scalar("boolean")

  object ObjType extends Obj {
    override def fields: List[Field]     = Nil
    override val `type`: List[ValueType] = Nil
  }

  abstract class ArrayLike(val element: Type) extends Type {
    override val `type`: List[ValueType] = element.`type`
  }

  object ArrayLike {
    def unapply(arg: ArrayLike): Option[Type] = Some(arg.element)
  }

  case class Array(override val element: Type) extends ArrayLike(element)

  case class SortedArray(override val element: Type) extends ArrayLike(element)

  object Any extends Type {
    override val `type`: List[ValueType] = List(Xsd + "anyType")
  }

}
