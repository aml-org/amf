package amf.metadata

import amf.vocabulary.Namespace.Xsd
import amf.vocabulary.ValueType

/**
  *
  */
trait Type {
  val `type`: List[ValueType]

  val dynamicType: Boolean  = false
}

trait Obj extends Type {

  // This can be override by dynamic element models to provide
  // the information about fields at parsing time

  val dynamic: Boolean = false

  def fields: List[Field]
}

object Type {

  case class Scalar(id: String) extends Type {
    override val `type`: List[ValueType] = List(Xsd + id)
  }

  object Str extends Scalar("string")

  object Int extends Scalar("int")

  object Iri extends Scalar("url")

  object Bool extends Scalar("boolean")

  object RegExp extends Scalar("token")

  object ObjType extends Obj {
    override val fields: List[Field]     = Nil
    override val `type`: List[ValueType] = Nil
  }

  case class Array(element: Type) extends Type {
    override val `type`: List[ValueType] = element.`type`
  }

  case class SortedArray(element: Type) extends Type {
    override val `type`: List[ValueType] = element.`type`
  }

  object Any extends Type {
    override val `type`: List[ValueType] = List(Xsd + "anyType")
  }

}
