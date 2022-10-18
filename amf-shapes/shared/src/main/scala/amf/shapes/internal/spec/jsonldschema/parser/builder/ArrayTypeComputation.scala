package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Type
import amf.core.internal.metamodel.Type.{Any, Scalar}
import amf.shapes.internal.spec.jsonldschema.parser.builder.ListOps.sameSize

import scala.annotation.tailrec
import scala.collection.SeqLike

object ArrayTypeComputation {

  def computeType(prev: Type, next: Type): Type = {
    (prev, next) match {
      case (Type.Array(prev), Type.Array(next)) => Type.Array(computeType(prev, next))
      case DifferentScalarType(any)             => any
      case SameIriType(next)                    => next
      case DifferentIriType(next)               => next
      case _                                    => Any
    }
  }
}

object DifferentScalarType {
  def unapply(tuple: (Type, Type)): Option[Type] = tuple match {
    case (Scalar(prevType), Scalar(nextType)) if prevType != nextType => Some(Any)
    case _                                                            => None
  }
}

object SameIriType {
  def unapply(tuple: (Type, Type)): Option[Type] = {
    val (prev, next) = tuple
    val intersection = prev.typeIris.intersect(next.typeIris)
    if (sameSize(intersection, prev.typeIris, next.typeIris)) Some(prev)
    else None
  }
}

object DifferentIriType {
  def unapply(tuple: (Type, Type)): Option[Type] = {
    val (prev, next) = tuple
    val intersection = prev.typeIris.intersect(next.typeIris)
    if (!sameSize(intersection, prev.typeIris, next.typeIris) && intersection.nonEmpty)
      Some(AnonObj(intersection.map(ValueType(_))))
    else None
  }
}

object ListOps {
  def sameSize(seqs: SeqLike[_, _]*): Boolean = {
    val size = seqs.head.size
    !seqs.exists(_.size != size)
  }
}

object AnonObj {
  def apply(`type`: ValueType*): AnonObj = AnonObj(`type`.toList)
}

case class AnonObj(`type`: List[ValueType]) extends Type
