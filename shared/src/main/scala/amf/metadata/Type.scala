package amf.metadata

import amf.vocabulary.Namespace.Xsd
import amf.vocabulary.ValueType

/**
  *
  */
trait Type {
  val `type`: List[ValueType]
}

trait Obj extends Type {
  val fields: List[Field]
}

object Type {

  case class Scalar(id: String) extends Type {
    override val `type`: List[ValueType] = List(Xsd + id)
  }

  object Str extends Scalar("string")

  object Iri extends Scalar("url")

  object Bool extends Scalar("boolean")

  object RegExp extends Scalar("token")

  case class Array(element: Type) extends Type {
    override val `type`: List[ValueType] = element.`type`
  }
}
