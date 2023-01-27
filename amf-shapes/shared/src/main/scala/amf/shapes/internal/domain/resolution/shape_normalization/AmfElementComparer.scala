package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.{AmfElement, AmfScalar}
import amf.core.internal.metamodel.Field

object AmfElementComparer {

  object BothString extends Extractor[String] {
    def unapply(tuple: (AmfElement, AmfElement)): Option[(String, String)] = {
      tuple match {
        case (left: AmfScalar, right: AmfScalar) => unapply(left, right)
        case _                                   => None
      }
    }

    def unapply(left: AmfScalar, right: AmfScalar): Option[(String, String)] = {
      Some(left.toString(), right.toString())
    }
  }

  object BothNumeric extends Extractor[Number] {
    def unapply(tuple: (AmfElement, AmfElement)): Option[(Number, Number)] = {
      tuple match {
        case (left: AmfScalar, right: AmfScalar) => unapply(left, right)
        case _                                   => None
      }
    }

    def unapply(left: AmfScalar, right: AmfScalar): Option[(Number, Number)] = {
      for {
        leftNum  <- left.toNumberOption
        rightNum <- right.toNumberOption
      } yield {
        (leftNum, rightNum)
      }
    }
  }

  object BothBoolean extends Extractor[Boolean] {
    def unapply(tuple: (AmfElement, AmfElement)): Option[(Boolean, Boolean)] = {
      tuple match {
        case (left: AmfScalar, right: AmfScalar) => unapply(left, right)
        case _                                   => None
      }
    }

    def unapply(left: AmfScalar, right: AmfScalar): Option[(Boolean, Boolean)] = {
      for {
        leftBool  <- toBoolOption(left)
        rightBool <- toBoolOption(right)
      } yield {
        (leftBool, rightBool)
      }
    }

    def toBoolOption(scalar: AmfScalar): Option[Boolean] = {
      Option(scalar.value) match {
        case Some(v: String) => Some(v.toBoolean)
        case Some(true)      => Some(true)
        case Some(false)     => Some(false)
        case _               => None
      }
    }
  }

  type IncompatibilityError = (String) => InheritanceIncompatibleShapeError
  type ErrorFactory         = () => InheritanceIncompatibleShapeError

  trait Extractor[T] {
    def unapply(tuple: (AmfElement, AmfElement)): Option[(T, T)]
  }

  trait CompareCriteria[T] {
    def compare(left: T, right: T): Boolean
  }

  def areEqualStrings(left: AmfElement, right: AmfElement, onError: IncompatibilityError) = {
    compare(BothString, (l: String, r: String) => l == r)(
      left,
      right,
      () => onError("Cannot compare non numeric or missing values")
    )
  }

  def areEqualBooleans(left: AmfElement, right: AmfElement, onError: IncompatibilityError) = {
    compareBooleans(left, right, (l: Boolean, r: Boolean) => l == r, onError)
  }

  def areExpectedBooleans(
      left: AmfElement,
      right: AmfElement,
      expectedLeft: Boolean,
      expectedRight: Boolean,
      onError: IncompatibilityError
  ) = {
    compareBooleans(left, right, (l: Boolean, r: Boolean) => l == expectedLeft && r == expectedRight, onError)
  }

  def compareBooleans(
      left: AmfElement,
      right: AmfElement,
      comparator: CompareCriteria[Boolean],
      onError: IncompatibilityError
  ) = {
    compare(BothBoolean, comparator)(left, right, () => onError("Cannot compare non numeric or missing values"))
  }

  def lessOrEqualThan(left: AmfElement, right: AmfElement, onError: IncompatibilityError) = {
    compareNumbers(left, right, (l: Number, r: Number) => l.intValue() <= r.intValue(), onError)
  }

  def moreOrEqualThan(left: AmfElement, right: AmfElement, onError: IncompatibilityError) = {
    compareNumbers(left, right, (l: Number, r: Number) => l.intValue() >= r.intValue(), onError)
  }

  def compareNumbers(
      left: AmfElement,
      right: AmfElement,
      comparator: CompareCriteria[Number],
      onError: IncompatibilityError
  ) = {
    compare(BothNumeric, comparator)(left, right, () => onError("Cannot compare non numeric or missing values"))
  }

  def compare[T](
      extractor: Extractor[T],
      comparator: CompareCriteria[T]
  )(left: AmfElement, right: AmfElement, onError: ErrorFactory): Boolean = {
    (left, right) match {
      case extractor(lValue, rValue) =>
        comparator.compare(lValue, rValue)
      case _ =>
        throw onError()
    }
  }

  def incompatibleException(field: Field, annotable: AmfElement) = (message: String) => {
    new InheritanceIncompatibleShapeError(message, Some(field.value.iri()), annotable.location(), annotable.position())
  }
}
