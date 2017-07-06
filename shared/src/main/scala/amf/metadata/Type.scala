package amf.metadata

/**
  * Created by pedro.colunga on 7/5/17.
  */
trait Type

object Type {

  object Str extends Type

  object Bool extends Type

  object Enum extends Type

  object RegExp extends Type

  case class Array(element: Type) extends Type
}
